(ns igniteinator.text
  (:require [igniteinator.state :refer [language]]
            [reagent.core :as r]))

(defonce strings {
                  :data-load-error {:en "Error loading data"}
                  })
(defn txt [s]
  (get-in strings [s @language]))
