(ns igniteinator.ui.pages.card-details
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.card-list :refer [card-list card-grid]]
            [igniteinator.text :refer [txt txt-c]]
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.modal :refer [modal]]
            [reagent-mui.material.box :refer [box]]))

(defn combos-list [card]
  (let [cards (<sub :card-details-page/combos)]
    [card-list
     {:on-click-fn (fn [c]
                     #(>evt :show-card-details c :page/replace))
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

(defn card-details-page []
  (let [card (<sub :card-details-page/card)]
    [page (str (txt :card-details-page-title) " " (:name card))
     [box {:mb 2} [back-button]]
     ;; Show the next step larger than the card list.
     [grid {:container true}
      [card-grid {:component            "div"
                  :grid-breakpoints-ref (<sub-ref :grid-breakpoints+1)
                  :display-name?        false}
       card]]
     (if (empty? (:combos card))
       [:p (txt :no-combos)]
       [combos-section card])]))
