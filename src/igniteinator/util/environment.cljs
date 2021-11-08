(ns igniteinator.util.environment
  (:require [clojure.string :as str]))

(defn user-agent []
  (condp re-find (str/lower-case (.. js/self -navigator -userAgent))
    #"iphone|ipad|ipod" :ios
    :other))

(defn standalone-mode? []
  (or
    (.. js/self -navigator -standalone)
    (-> js/self (.matchMedia "(display-mode: standalone)") .-matches)))
