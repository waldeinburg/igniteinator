(ns igniteinator.ui.cards-page
  (:require [igniteinator.state :refer [state set-in-state! assoc-a!]]
            [igniteinator.ui.base-filtering :refer [base-filtering]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.model.cards :refer [get-cards]]
            [igniteinator.text :refer [txt]]
            [reagent.core :as r]))

(defonce current-page (r/cursor state [:current-page]))
(defonce page-state (r/cursor state [:cards-page]))
(defonce card-selection-cursor (r/cursor page-state [:card-selection]))

(defn show-combos [card]
  (set-in-state!
    [:current-page] :combos
    [:previous-page] :cards
    [:combos-page :card-id] (:id card)))

(defn cards-page []
  (when (= :cards @current-page)
    [page (txt :cards-page-title)
     [base-filtering
      {:selected-value        (if (= :all (:base @page-state))
                                :all
                                :some)
       :on-change             #(case (keyword %2)
                                 :all (assoc-a! page-state :base :all)
                                 :some (assoc-a! page-state :base @card-selection-cursor))
       :card-selection-cursor card-selection-cursor}]
     [card-list
      {:on-click-fn (fn [card]
                      (when (not-empty (:combos card))
                        #(show-combos card)))
       :tooltip-fn  (fn [card]
                      (if (not-empty (:combos card))
                        (txt :show-combos)
                        (txt :no-combos)))}
      (get-cards (:base @page-state) (:filters @page-state) (:sortings @page-state))]]))
