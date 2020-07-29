(defproject csv2sql "0.1.4-SNAPSHOT"
  :description "A simple ETL tool to load CSVs in a directory tree into a SQL database (MySQL/PostgreSQL/SQLite). Based on 'https://github.com/ivarthorson/csv2sql'"
  :url "http://github.com/go-choppy/csv2sql"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.postgresql/postgresql "42.2.5"]
                 [commons-io "2.6"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [clojure.java-time "0.3.2"]
                 [lambdaisland/uri "1.2.1"]
                 [org.clojure/tools.logging "0.5.0"
                  :exclusions [org.clojure/clojure]]
                 [clj-http "3.10.1"]]

  :repositories [["central" "https://maven.aliyun.com/repository/central"]
                 ["jcenter" "https://maven.aliyun.com/repository/jcenter"]
                 ["clojars" "https://mirrors.tuna.tsinghua.edu.cn/clojars/"]]

  :plugin-repositories [["central" "https://maven.aliyun.com/repository/central"]
                        ["jcenter" "https://maven.aliyun.com/repository/jcenter"]
                        ["clojars" "https://mirrors.tuna.tsinghua.edu.cn/clojars/"]]

  :repl-options {:init-ns csv2sql.core}
  :main csv2sql.core
  :aot [csv2sql.core]
  :plugins [[io.taylorwood/lein-native-image "0.3.1"]])

