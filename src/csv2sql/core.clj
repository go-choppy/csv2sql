(ns csv2sql.core
  (:gen-class)
  (:require [clojure.data.csv]
            [clojure.edn :as edn]
            [clojure.java.jdbc :as sql]
            [csv2sql.guess-schema :as guess]
            [csv2sql.files :as files]
            [csv2sql.json :as json]
            [csv2sql.csvs :as csv]
            [clojure.java.io :as io]
            [csv2sql.util :as util]
            [clojure.string :as clj-str]
            [csv2sql.notification :as notification]
            [csv2sql.dingtalk :refer [setup-access-token setup-secret send-link-msg!]]))

(defn table-schema-file
  [^java.io.File dir]
  (io/file (format "%s-schema.edn" (.getAbsolutePath dir))))

(defn table-sql-file
  [^java.io.File dir]
  (io/file (format "%s.sql" (.getAbsolutePath dir))))

(defn matching-csv-for-json
  [^java.io.File jsonfile]
  (io/file (format "%s.csv" (.getAbsolutePath jsonfile))))

(defn convert-jsons-to-csvs!
  "Scans through the subdirectories of CSVDIR, and for each JSON that exists,
  creates a .csv file (if it does not already exist) with the same contents."
  [csvdir]
  (printf "Converting JSONs into CSVs: %s\n" csvdir)
  (flush)
  (doseq [dir (files/list-subdirectories csvdir)]
    (doseq [jsonfile (files/list-files-of-type dir "json")]
      (let [csvfile ^java.io.File (matching-csv-for-json jsonfile)]
        (when-not (.isFile csvfile)
          (let [filepath (.getAbsolutePath ^java.io.File jsonfile)]
            (try (-> filepath
                     (json/load-json-as-csv)
                     (csv/save-csv csvfile))
                 (catch Exception e
                   (println "ERROR LOADING: " filepath)))))))))

(defn autodetect-sql-schemas!
  "Scans through the subdirectories of CSVDIR, infers the column data types,
  and stores the inferred schema in CSVDIR so that you may manually edit it
  before loading it in with MAKE-SQL-TABLES."
  [csvdir & strict-mode?]
  (doseq [dir (files/list-subdirectories csvdir)]
    ; https://clojuredocs.org/clojure.core/printf#example-542692d4c026201cdc327038
    (printf "Autodetecting schema for: %s\n" dir)
    (flush)
    (let [tablename (.getName ^java.io.File dir)
          schema (guess/scan-csvdir-and-make-schema dir strict-mode?)]
      (when-not (empty? schema)
        (let [table-sql (guess/table-definition-sql-string tablename schema)]
          (println (table-schema-file dir) schema)
          (spit (table-schema-file dir) schema)
          (spit (table-sql-file dir) table-sql))))))


(def default-db
  (let [db-type (System/getenv "DATABASE_TYPE")]
    (cond
      (= db-type "postgresql") {:dbtype "postgresql"
                                :host     (or (System/getenv "POSTGRES_HOST") "localhost")
                                :port     (or (System/getenv "POSTGRES_PORT") "5432")
                                :dbname   (or (System/getenv "POSTGRES_DB")  "csv2sql")
                                :user     (or (System/getenv "POSTGRES_USER") "postgres")
                                :password (or (System/getenv "POSTGRES_PASS") "mysecretpassword")}
      (= db-type "mysql") {:dbtype "mysql"
                           :host     (or (System/getenv "MYSQL_HOST") "localhost")
                           :port     (or (System/getenv "MYSQL_PORT") "3306")
                           :dbname   (or (System/getenv "MYSQL_DB")  "csv2sql")
                           :user     (or (System/getenv "MYSQL_USER") "mysql")
                           :password (or (System/getenv "MYSQL_PASS") "mysecretpassword")}
      :else {:classname   "org.sqlite.JDBC"
             :subprotocol "sqlite"
             :subname     (or (System/getenv "SQLITE_DB_PATH") "sqlite-database.db")})))

(defn connection-ok?
  "A predicate that tests if the database is connected."
  [db]
  (= {:result 15} (first (sql/query db ["select 3*5 as result"]))))

(defn drop-existing-sql-tables!
  "For each subdirectory in DIRNAME, drop any tables with the same name."
  [db csvdir]
  (doseq [table-name (map (fn [f] (.getName ^java.io.File f))
                          (files/list-subdirectories csvdir))]
    (let [cmd (format "DROP TABLE IF EXISTS %s;" table-name)]
      (sql/db-do-commands db cmd))))

(defn make-sql-tables!
  "Makes the SQL tables from whatever is in the database. "
  [db csvdir]
  (doseq [sql-file (map (fn [f] (.getAbsolutePath ^java.io.File f))
                        (files/list-files-of-type csvdir "sql"))]
    (println sql-file)
    (let [table-sql (slurp sql-file)]
      (println table-sql)
      (sql/db-do-commands db table-sql))))

(defn insert-csv!
  "Inserts the rows of the CSV into the database, converting the rows to the appropriate
  type as they are loaded. Lazy, so it works on very large files. If a column is not
  found in the schema, it is omitted and not inserted into the database. "
  [db table csvfile schema]
  (with-open [reader (util/bom-reader csvfile)]
    (let [sep (csv/guess-separator csvfile)
          csv-rows (clojure.data.csv/read-csv reader :separator sep)
          [header typed-rows] (guess/parse-csv-rows-using-schema schema csv-rows)
          cnt (atom 0)
          chunk-size 1000]
      (doseq [chunk-of-rows (partition-all chunk-size typed-rows)]
        (let [line-num (swap! cnt inc)]
          (print ".")
          (flush))
        (sql/insert-multi! db table header chunk-of-rows)))))

(defn insert-all-csvs!
  "Loads all the subdirectories of DATA_DIR as tables. Optional hashmap MANUAL-OPTIONS
  lets you decide how to customize various tables; for example, you may want to set
  an optional table."
  [db csvdir]
  (doseq [dir (files/list-subdirectories csvdir)]
    (let [tablename (.getName ^java.io.File dir)
          schema (edn/read-string (slurp (table-schema-file dir)))]
      (when-not (empty? schema)
        (doseq [csvfile (files/list-files-of-type dir "csv|tsv|txt")]
          (print (format "\nLoading: %s " csvfile))
          (insert-csv! db tablename csvfile schema))))))

(defn -main
  []
  ; TODO: Add schema for checking environment variables.
  (let [csvdir (System/getenv "DATA_DIR")
        db default-db
        notification? (= (System/getenv "NOTIFICATION_ENABLED") "true")
        notification-plugin (or (System/getenv "NOTIFICATION_PLUGIN") "metabase")
        notification-url (System/getenv "NOTIFICATION_URL")
        dataset-id (System/getenv "DATASET_ID")
        notification-types (clj-str/split (System/getenv "NOTIFICATION_TYPES") #",")
        auth-type (or (System/getenv "AUTH_TYPE") "cookie")
        auth-key (System/getenv "AUTH_KEY")
        auth-value (System/getenv "AUTH_VALUE")
        access-token (System/getenv "ACCESS_TOKEN")
        secret (System/getenv "SECRET")
        username (System/getenv "USERNAME")
        dbname (:dbname db)
        strict-mode? (= (System/getenv "STRICT_MODE") "true")]
    (when-not (and csvdir (util/exists? csvdir))
      (throw (Exception. "Please specify a valid DATA_DIR environment variable and ensure it exists.")))
    (when-not (connection-ok? db)
      (throw (Exception. (str "Unable to connect to DB:" db))))
    ; Notification: I'll update XXX dataset
    (when notification?
      (when (and access-token username)
        (do
          (setup-access-token access-token)
          (setup-secret secret)
          (send-link-msg! "Metabase's Update Notification"
                          (format "%s Will Update Dataset %s, Please Click the Card for More Details." (clj-str/capitalize username) dbname)
                          "https://nordata-cdn.oss-cn-shanghai.aliyuncs.com/choppy/running.jpeg"
                          (str notification-url "/browse/" dataset-id)))))
    (drop-existing-sql-tables! db csvdir)
    (convert-jsons-to-csvs! csvdir)
    (autodetect-sql-schemas! csvdir strict-mode?)
    (make-sql-tables! db csvdir)
    (insert-all-csvs! db csvdir)
    (println "Done!")
    ; Updated dataset
    (when notification?
      ; Get session id by using username and password
      (let [auth (notification/metabase-auth notification-url auth-key auth-value)]
        (doseq [type notification-types]
          (notification/send-notification!
           (notification/metabase-notification-url notification-url dataset-id type)
           auth-type
           (:auth-key auth)
           (:auth-value auth)))
        (when (and access-token username)
          (send-link-msg! "Metabase's Updated Notification"
                          (format "%s Updated Dataset %s, Please Click the Card for New Data." (clj-str/capitalize username) dbname)
                          "http://metabase.3steps.cn/app/assets/img/apple-touch-icon.png"
                          (str notification-url "/browse/" dataset-id)))))
    (System/exit 0)))
