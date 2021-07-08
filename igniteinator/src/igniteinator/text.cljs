(ns igniteinator.text
  (:require [igniteinator.state :refer [language]]))

(defonce strings
  {
   :back                      {:en "Back"}
   :data-load-error           {:en "Error loading data"}
   :show-combos               {:en "Show known combos"}
   :no-combos                 {:en "No known combos for this card"}
   :no-more-combos            {:en "No more known combos for this card"}
   :cards-page-title          {:en "Cards"}
   :combos-page-title         {:en "Combos for"}
   :card-selection            {:en "Card selection"}
   :select-all                {:en "All"}
   :select-some               {:en "Select cards"}
   :select-cards-dialog-title {:en "Select cards"}
   })

(defn txt [s]
  (get-in strings [s @language]))
