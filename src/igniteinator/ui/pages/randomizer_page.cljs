(ns igniteinator.ui.pages.randomizer-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.material.alert :refer [alert]]
            [reagent-mui.material.button :refer [button]]))

(defn randomizer-page []
  (let [market          (<sub :randomizer/market)
        display         (<sub :randomizer/display)
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
                :sx       {:mb       2
                           :opacity  1
                           :position :sticky
                           :top      16
                           :z-index  999}}
         "There are cards with unresolved dependencies. Please replace marked cards or replace another card to fix."])
      (if market
        [card-list
         {:tooltip          "Replace card"
          :href-fn          false
          :on-click-fn      (if (= :specs display)
                              ;; Not if display is :sorted. Replacing cards in this mode will more often than not result
                              ;; in a reordering of the cards (because a card is replaced by a different cost) which
                              ;; makes it seem like multiple cards are replaced. Also, it makes it less transparent why
                              ;; a certain card is selected.
                              ;; The alternative would be to make an option that shows the spec name under the card and
                              ;; a sort button. I'm not sure that would make it more user-friendly.
                              (fn [card]
                                #(>evt :randomizer/replace-card filter-utils specs (:spec-idx card))))
          :content-below-fn (if (= :specs display)
                              (fn [card]
                                (:spec-name card)))
          :card-image-sx-fn (fn [card]
                              (if (:unresolved? card)
                                {:background-color "rgb(255, 0, 0)"
                                 :opacity          0.4}))}
         market]))))
