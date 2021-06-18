(ns igniteinator.text
  (:require [igniteinator.state :refer [state]]
            [reagent.core :as r]))

(defonce strings {
                  :data-load-error {:en "Error loading data"}
                  })

(let [language (r/cursor state [:language])]
  (defn txt [s]
    (get-in strings [s @language])))
