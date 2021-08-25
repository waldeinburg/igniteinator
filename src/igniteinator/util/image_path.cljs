(ns igniteinator.util.image-path
  (:require [igniteinator.constants :as constants]))

(defn image-path [language card]
  (str constants/gen-img-base-path "/" (name language) "/" (:id card) constants/gen-img-ext))
