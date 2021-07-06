(ns igniteinator.ui.cards-page
  (:require [igniteinator.state :refer [state set-in-state!]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.model.cards :refer [get-cards]]
            [igniteinator.text :refer [txt]]
            [reagent.core :as r]))

(defn show-combos [card]
  (set-in-state!
    [:current-page] :combos
    [:previous-page] :cards
    [:combos-page :card-id] (:id card)))

(let [current-page (r/cursor state [:current-page])
      page-state   (r/cursor state [:cards-page])]
  (defn cards-page []
    (when (= :cards @current-page)
      [page (txt :cards-page-title)
       [card-list
        {:on-click-fn (fn [card]
                        (when (not-empty (:combos card))
                          #(show-combos card)))
         :tooltip-fn  (fn [card]
                        (if (not-empty (:combos card))
                          (txt :show-combos)
                          (txt :no-combos)))}
        (get-cards (:base @page-state) (:filters @page-state) (:sortings @page-state))]])))
