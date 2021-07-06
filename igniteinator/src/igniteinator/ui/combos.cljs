(ns igniteinator.ui.combos
  (:require [igniteinator.state :refer [state assoc!]]
            [igniteinator.model.cards :refer [get-card-from-id get-cards-from-ids]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.ui.back-button :refer [back-button]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.text :refer [txt]]
            [reagent.core :as r]
            [reagent-material-ui.core.container :refer [container]]
            [reagent-material-ui.core.modal :refer [modal]]))

(defonce page-state (r/cursor state [:combos-page]))

(defn combos-list [card]
  (let [cards (get-cards-from-ids (:combos card))]
    [card-list
     {:on-click-fn (fn [c]
                     (when (not-empty (:combos c))
                       #(assoc! page-state :card-id (:id c))))
      :tooltip-fn  (fn [c]
                     (case (:combos c)
                       [] (txt :no-combos)
                       [(:id card)] (txt :no-more-combos)
                       (txt :show-combos)))}
     cards]))

(let [current-page (r/cursor state [:current-page])]
  (defn combos-page []
    (when (= :combos @current-page)
      (let [c (get-card-from-id (:card-id @page-state))]
        [page (str (txt :combos-page-title) " " (:name c))
         [back-button]
         ;; Show main card in a card-list instead of a card-container to make size the same as the
         ;; cards in the list.
         [card-list [c]]
         [combos-list c]]))))
