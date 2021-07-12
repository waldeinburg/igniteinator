(ns igniteinator.text
  (:require [igniteinator.state :refer [language]]
            [clojure.string :as s]))

(defonce strings
  {
   :back                      {:en "back"}
   :select-all                {:en "select all"}
   :search                    {:en "search"}
   :using                     {:en "using"}
   :regular-expressions       {:en "regular expressions"}
   :clear-selection           {:en "clear selection"}
   :data-load-error           {:en "error loading data"}
   :show-combos               {:en "show known combos"}
   :no-combos                 {:en "no known combos for this card"}
   :no-more-combos            {:en "no more known combos for this card"}
   :cards-page-title          {:en "cards"}
   :combos-page-title         {:en "combos for"}
   :card-selection            {:en "card selection"}
   :select-all-button         {:en "all"}
   :select-some-button        {:en "select cards"}
   :select-cards-dialog-title {:en "select cards"}
   })

(defn txt [s]
  (get-in strings [s @language]))

(defn txt-c [s]
  (s/capitalize (txt s)))
