(ns igniteinator.ui.cards-page
  (:require [igniteinator.ui.base-filtering :refer [base-filtering]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.ui.search-bar :refer [search-bar]]
            [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.toolbar :refer [toolbar]]))

(defn page-base-filtering []
  [base-filtering
   {:dialog-open?-ref               (<sub-ref :cards-page.card-selection/dialog-open?)
    :<sub-dialog-item-selected?-ref #(<sub-ref :cards-page.card-selection/item-selected? %)
    :get-selected-value             #(let [base (<sub :cards-page/base)]
                                       (if (keyword? base)
                                         base
                                         :some))
    :on-dialog-change-open          #(>evt :cards-page.card-selection/set-dialog-open? %)
    :on-dialog-item-selected-change (fn [id set?]
                                      (>evt :cards-page.card-selection/set-item-selected? id set?))
    :on-dialog-selection-set        #(>evt :cards-page.card-selection/set-selection %)
    :on-change                      #(if %
                                       ;; Input will be nil if a button is clicked without being changed.
                                       (>evt :cards-page/set-base %))}])

(defn page-card-list []
  (let [cards (<sub :cards-page/cards)]
    [card-list
     {:on-click-fn (fn [card]
                     #(>evt :show-card-details card))
      :tooltip-fn  (fn [card]
                     (if (not-empty (:combos card))
                       (txt :card-tooltip-combos)
                       (txt :card-tooltip-no-combos)))}
     cards]))

(defn cards-page []
  [page (txt :cards-page-title)
   [toolbar {:disable-gutters true}
    [box {:mr 2}
     [page-base-filtering]]
    [search-bar
     (<sub-ref :cards-page/search-str)
     #(>evt :cards-page/set-search-str %)]]
   [page-card-list]])
