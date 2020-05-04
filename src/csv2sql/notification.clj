(ns csv2sql.notification
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn make-cookie
  [key value]
  {:cookies {key {:path "/", :value value}}})

(defn make-auth
  [auth-type auth-key auth-value]
  (when (= auth-type "cookie")
    (make-cookie auth-key auth-value)))

(defn send-notification!
  [url auth-type auth-key auth-value]
  (println "Send notification to " url)
  (client/post url (merge {:unexceptional-status #(<= 200 % 299)} (make-auth auth-type auth-key auth-value)))
  (println "Success."))

;; ------------------------------------------- Metabase ---------------------------------------------
(defn metabase-auth-url
  [base-url]
  ; http://metabase.3steps.cn/api/session
  (str base-url "/api/session"))

(defn metabase-auth
  [base-url username password]
  (let [body (:body (client/post (metabase-auth-url base-url)
                                 (merge {:unexceptional-status #(<= 200 % 299)}
                                        {:content-type :json
                                         :body (json/write-str {:username username :password password})})))
        session-id (:id (json/read-str body :key-fn keyword))]
    {:auth-key "metabase.SESSION"
     :auth-value session-id}))

(defn metabase-notification-url
  [base-url dataset notification-type]
  ; http://metabase.3steps.cn/api/database/6/rescan_values
  (str base-url "/api/database/" dataset "/" notification-type))
