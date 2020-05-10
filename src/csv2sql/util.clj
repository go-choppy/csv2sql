(ns csv2sql.util
  (:require [clojure.java.io :as io])
  (:import [org.apache.commons.io.input BOMInputStream]))

(set! *warn-on-reflection* true)

(defn alphanumeric?
  "TRUE when the string is completely alphanumeric."
  [string & strict-mode?]
  (let [pattern (if strict-mode? #"[a-z_0-9]" #"[a-z_A-Z0-9]")]
    (and (> (count string) 0)
         (= string (apply str (re-seq pattern string))))))

(defn spaces-to-underscores
  "Converts spaces to underscores."
  [string]
  (clojure.string/replace string #"\s" "_"))

(defn periods-to-underscores
  "Converts spaces to underscores."
  [string]
  (clojure.string/replace string #"\." "_"))

(defn bom-reader
  "Remove `Byte Order Mark` and return reader"
  [filepath]
  (-> filepath
      io/input-stream
      BOMInputStream.
      io/reader))

(defn exists?
  "TRUE when File or directory exists."
  [path]
  (.exists (io/file path)))