(ns csv2sql.notification
  (:require [clj-http.client :as client]))

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

(defn metabase-notification-url
  [base-url dataset notification-type]
  ; http://metabase.3steps.cn/api/database/6/rescan_values
  (str base-url "/api/database/" dataset "/" notification-type))
