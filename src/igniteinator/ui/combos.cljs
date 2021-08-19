(ns igniteinator.ui.combos
  (:require [igniteinator.state :refer [state assoc-a!]]
            [igniteinator.model.cards :refer [get-card-from-id get-cards]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.ui.back-button :refer [back-button]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.text :refer [txt-c]]
            [reagent.core :as r]
            [reagent-material-ui.core.container :refer [container]]
            [reagent-material-ui.core.modal :refer [modal]]))

(def page-state (r/cursor state [:combos-page]))

(defn combos-list [card]
  (let [cards (get-cards (:combos card) [] (:sortings @page-state))]
    [card-list
     {:on-click-fn (fn [c]
                     (when (not-empty (:combos c))
                       #(assoc-a! page-state :card-id (:id c))))
      :tooltip-fn  (fn [c]
                     (case (:combos c)
                       [] (txt-c :no-combos)
                       [(:id card)] (txt-c :no-more-combos)
                       (txt-c :show-combos)))}
     cards]))

(let [current-page (r/cursor state [:current-page])]
  (defn combos-page []
    (when (= :combos @current-page)
      (let [c (get-card-from-id (:card-id @page-state))]
        [page (str (txt-c :combos-page-title) " " (:name c))
         [back-button]
         ;; Show main card in a card-list instead of a card-container to make size the same as the
         ;; cards in the list.
         [card-list [c]]
         [combos-list c]]))))
