(ns igniteinator.util.user-agent
  (:require [clojure.string :as str]))

(defn user-agent []
  (condp re-find (str/lower-case (.. js/self -navigator -userAgent))
    #"iphone|ipad|ipod" :ios
    :other))
