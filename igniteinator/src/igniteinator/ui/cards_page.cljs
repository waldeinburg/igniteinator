(ns igniteinator.ui.cards-page
  (:require [igniteinator.state :refer [state]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.model.cards :refer [get-cards]]
            [igniteinator.text :refer [txt]]
            [reagent.core :as r]))

(defn show-combos [card]
  (println (:combos card)))

(let [page-state (r/cursor state [:cards-page])]
  (defn cards-page []
    [card-list
     {:on-click-fn (fn [card]
                     (when (not-empty (:combos card))
                       #(show-combos card)))
      :tooltip-fn  (fn [card]
                     (if (not-empty (:combos card))
                       (txt :show-combos)
                       (txt :no-combos)))}
     (get-cards (:base @page-state) (:filters @page-state) (:sortings @page-state))]))
