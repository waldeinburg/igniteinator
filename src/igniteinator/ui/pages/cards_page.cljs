(ns igniteinator.ui.pages.cards-page
  (:require [igniteinator.ui.components.base-filtering :refer [base-filtering]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.search-bar :refer [search-bar]]
            [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.toolbar :refer [toolbar]]))

(defn page-base-filtering []
  [base-filtering
   {:select-dialog-open?-ref        (<sub-ref :cards-page.card-selection/dialog-open?)
    :combos-dialog-open?-ref        (<sub-ref :cards-page.combos/dialog-open?)
    :<sub-dialog-item-selected?-ref #(<sub-ref :cards-page.card-selection/item-selected? %)
    :combos-value-ref               (<sub-ref :cards-page.combos/value)
    :get-selected-value             #(let [base (<sub :cards-page/base)]
                                       (if (keyword? base)
                                         base
                                         :some))
    :on-select-dialog-change-open   #(>evt :cards-page.card-selection/set-dialog-open? %)
    :on-combos-dialog-change-open   #(>evt :cards-page.combos/set-dialog-open? %)
    :on-combos-change               #(>evt :cards-page.combos/set-value %)
    :on-dialog-item-selected-change (fn [id set?]
                                      (>evt :cards-page.card-selection/set-item-selected? id set?))
    :on-dialog-selection-set        #(>evt :cards-page.card-selection/set-selection %)
    :on-change                      #(if %
                                       ;; Input will be nil if a button is clicked without being changed.
                                       (>evt :cards-page/set-base %))}])

(defn page-card-list []
  (let [cards (<sub :cards-page/cards)]
    [card-list cards]))

(defn cards-page []
  [page (txt :cards-page-title)
   [box {:mb 2}
    [toolbar {:disable-gutters true}
     [box {:mr 2}
      [page-base-filtering]]
     [search-bar
      (<sub-ref :cards-page/search-str)
      #(>evt :cards-page/set-search-str %)]]]
   [page-card-list]])
