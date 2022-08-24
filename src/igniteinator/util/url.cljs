(ns igniteinator.util.url
  (:require [clojure.string :as s]))

(defn to-param-str [p]
  (-> p
    (s/replace " " "_")
    (s/replace #"['!?]" "")))

(defn to-query-array [coll]
  (s/join "-" coll))
