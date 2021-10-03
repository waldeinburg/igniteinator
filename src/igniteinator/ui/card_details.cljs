(ns igniteinator.ui.card-details
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.ui.back-button :refer [back-button]]
            [igniteinator.ui.card-list :refer [card-list card-grid]]
            [igniteinator.text :refer [txt txt-c]]
            [reagent-material-ui.core.grid :refer [grid]]
            [reagent-material-ui.core.modal :refer [modal]]))

(defn combos-list [card]
  (let [sortings (<sub :card-details-page/sortings)
        cards    (<sub :cards (:combos card) [] sortings)]
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
     [back-button]
     ;; Just show in full size.
     [grid {:container true}
      [card-grid {:component            "div"
                  :grid-breakpoints-ref (<sub-ref :grid-breakpoints+1)}
       card]]
     (if (empty? (:combos card))
       [:p (txt-c :no-combos)]
       [combos-section card])]))
