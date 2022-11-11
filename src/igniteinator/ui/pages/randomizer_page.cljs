(ns igniteinator.ui.pages.randomizer-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.material.alert :refer [alert]]
            [reagent-mui.material.button :refer [button]]))

(defn randomizer-page []
  (let [selected-cards  (<sub :randomizer/selected-cards)
        some-unresolved (<sub :randomizer/some-unresolved)
        filter-utils    (<sub :filter-utils)
        specs           (<sub :randomizer/specs)
        card-ids        (<sub :randomizer/card-ids-to-shuffle)]
    (page (txt :randomizer/page-title)
      [button {:variant  :contained
               :sx       {:mb 2}
               :on-click #(>evt :randomizer/generate-market filter-utils specs card-ids)}
       "Generate market"]
      (if some-unresolved
        [alert {:severity :warning
                :sx       {:mb 2
                           :opacity 1
                           :position :sticky
                           :top 16
                           :z-index 999}}
         "There are cards with unresolved dependencies. Please replace marked cards or replace another card to fix."])
      (if selected-cards
        [card-list
         {:tooltip          "Replace card"
          :href-fn          false
          :on-click-fn      (fn [card]
                              #(>evt :randomizer/replace-card filter-utils specs (:idx card)))
          :container-sx-fn  (fn [card]
                              (if (:unresolved? card)
                                {:background-color "rgb(255, 0, 0)"}))
          :card-image-sx-fn (fn [card]
                              (if (:unresolved? card)
                                {:opacity 0.4}))}
         selected-cards]))))
