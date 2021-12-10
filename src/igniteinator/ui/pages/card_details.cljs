(ns igniteinator.ui.pages.card-details
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.ui.components.page-with-navigation :refer [page-with-navigation]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.card-list :refer [card-list card-grid]]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.vendor.swipeable-views :refer [swipeable-views]]
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.modal :refer [modal]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.fade :refer [fade]]))

(defn combos-list [card]
  (let [cards (<sub :card-details-page/combos card)]
    [card-list
     {:on-click-fn (fn [c]
                     #(>evt :show-card-details cards (:idx c) :page/replace))
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

(defn card-details [card-id]
  (let [card (<sub :card card-id)]
    ;; Show the next step larger than the card list.
    [:<>
     [grid {:container true}
      [card-grid {:component            "div"
                  :grid-breakpoints-ref (<sub-ref :grid-breakpoints+1)
                  :display-name?        false}
       card]]
     (if (empty? (:combos card))
       [:p (txt :no-combos)]
       [combos-section card])]))

(defn card-details-page []
  (let [card-ids (<sub :card-details-page/card-ids)]
    [page-with-navigation
     {:idx-ref                  (<sub-ref :card-details-page/idx)
      :current-title-ref        (<sub-ref :card-details-page/current-card-name)
      :previous-title-ref       (<sub-ref :card-details-page/previous-card-name)
      :first-transition-in?-ref (<sub-ref :card-details-page/first-transition-in?)
      :on-change-index          #(>evt :card-details-page/set-idx %)}
     (for [id card-ids]
       ^{:key id}
       [card-details id])]))
