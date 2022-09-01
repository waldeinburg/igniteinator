(ns igniteinator.ui.components.card-details
  (:require [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.card-list :refer [card-grid card-list]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.material.grid :refer [grid]]))

(defn combos-list [card]
  (let [cards (<sub :card-details-page/combos card)]
    [card-list
     {:on-click-fn (fn [c]
                     #(>evt :show-card-details cards (:idx c) :page/to-other-sub-page))
      :tooltip-fn  (fn [c]
                     (case (:combos c)
                       [] (txt :card-tooltip-no-combos)
                       [(:id card)] (txt :card-tooltip-no-more-combos)
                       (txt :card-tooltip-combos)))}
     cards]))

(defn combos-section [card]
  [:<>
   [:h3 (txt :combos-title)]
   [combos-list card]])

(defn card-details [card]
  ;; Show the next step larger than the card list.
  [:<>
   [grid {:container true}
    [card-grid {:component            "div"
                :grid-breakpoints-ref (<sub-ref :grid-breakpoints+1)
                :display-name?        false}
     card]]
   (if (empty? (:combos card))
     [:p (txt :no-combos)]
     [combos-section card])])

(defn card-details-from-id [card-id]
  [card-details (<sub :card card-id)])
