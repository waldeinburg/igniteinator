(ns igniteinator.ui.pages.randomizer-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.material.button :refer [button]]))

(defn randomizer-page []
  (let [selected-cards (<sub :randomizer/selected-cards)
        filter-utils   (<sub :filter-utils)
        specs          (<sub :randomizer/specs)
        card-ids       (<sub :randomizer/card-ids-to-shuffle)]
    (page (txt :randomizer/page-title)
      [button {:variant  :contained
               :sx       {:mb 2}
               :on-click #(>evt :randomizer/generate-market filter-utils specs card-ids)}
       "Generate market"]
      (if selected-cards
        [card-list
         {:tooltip     "Replace card"
          :href-fn     false
          :on-click-fn (fn [card]
                         #(>evt :randomizer/replace-card filter-utils specs (:idx card)))}
         selected-cards]))))
