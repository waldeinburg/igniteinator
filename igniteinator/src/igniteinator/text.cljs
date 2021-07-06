(ns igniteinator.text
  (:require [igniteinator.state :refer [language]]))

(defonce strings
  {
   :data-load-error {:en "Error loading data"}
   :show-combos     {:en "Show known combos"}
   :no-combos       {:en "No known combos for this card"}
   })

(defn txt [s]
  (get-in strings [s @language]))
