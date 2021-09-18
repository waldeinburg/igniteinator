(ns igniteinator.ui.card-details
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.ui.back-button :refer [back-button]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.text :refer [txt-c]]
            [reagent-material-ui.core.container :refer [container]]
            [reagent-material-ui.core.modal :refer [modal]]))

(defn combos-list [card]
  (let [sortings (<sub :card-details-page/sortings)
        cards    (<sub :cards (:combos card) [] sortings)]
    [card-list
     {:on-click-fn (fn [c]
                     #(>evt :card-details-page/set-card-id (:id c)))
      :tooltip-fn  (fn [c]
                     (case (:combos c)
                       [] (txt-c :no-combos)
                       [(:id card)] (txt-c :no-more-combos)
                       (txt-c :show-combos)))}
     cards]))

(defn card-details-page []
  (when (= :card-details (<sub :current-page))
    (let [card-id (<sub :card-details-page/card-id)
          c       (<sub :card card-id)]
      [page (str (txt-c :combos-page-title) " " (:name c))
       [back-button]
       ;; Show main card in a card-list instead of a card-container to make size the same as the
       ;; cards in the list.
       [card-list [c]]
       [combos-list c]])))
