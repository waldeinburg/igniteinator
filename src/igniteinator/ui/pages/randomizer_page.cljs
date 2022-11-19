(ns igniteinator.ui.pages.randomizer-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.bool-input :refer [switch]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.event :as event]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.icons.done :refer [done] :rename {done done-icon}]
            [reagent-mui.icons.edit :refer [edit] :rename {edit edit-icon}]
            [reagent-mui.icons.shuffle :refer [shuffle] :rename {shuffle shuffle-icon}]
            [reagent-mui.material.alert :refer [alert]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.menu-item :refer [menu-item]]
            [reagent-mui.material.select :refer [select]]
            [reagent.core :as r]))

(defn generate-market-button []
  (let [generated?   (<sub :randomizer/market-generated?)
        filter-utils (<sub :filter-utils)
        specs        (<sub :randomizer/specs)
        card-ids     (<sub :randomizer/card-ids-to-shuffle)]
    [button {:variant    (if generated? :outlined :contained)
             :start-icon (r/as-element [shuffle-icon])
             :sx         {:mb 2}
             :on-click   #(>evt :randomizer/generate-market filter-utils specs card-ids)}
     "Generate market"]))

(defn unresolved-alert []
  (if (<sub :randomizer/some-unresolved)
    [alert {:severity :warning
            :sx       {:mb       2
                       :opacity  1
                       :position :sticky
                       :top      16
                       :z-index  999}}
     "There are cards with unresolved dependencies. Please replace marked cards or replace another card to fix."]))

(defn market-toolbar []
  (let [edit?                 (<sub :randomizer/edit?)
        sort-button-disabled? (<sub :randomizer/sort-button-disabled?)
        replace-using-specs?  (<sub :randomizer/replace-using-specs?)
        default-sortings      (<sub :default-order-sortings)]
    [box {:sx {:mb 2}}
     [box {:sx {:mb        2,
                :display   :flex
                :flex-wrap :wrap
                :row-gap   1}}
      (if edit?
        [button {:variant    :contained
                 :start-icon (r/as-element [done-icon])
                 :on-click   #(>evt :randomizer/edit-done)}
         "Done"]
        [button {:variant    :outlined
                 :start-icon (r/as-element [edit-icon])
                 :on-click   #(>evt :randomizer/edit-start default-sortings)}
         "Edit"])
      [switch {:wrapper-sx   {:ml 1, :mr 1}
               :label        "Show rules"
               :checked?-ref (<sub-ref :randomizer/show-specs?)
               :on-change    #(>evt :randomizer/update-show-specs? default-sortings %)}]
      (if edit?
        [:<>
         [button {:sx       {:mr 2}
                  :disabled sort-button-disabled?
                  :on-click #(>evt :randomizer/update-order default-sortings)}
          "Sort"]
         [form-item {:label "Replace card with"}
          [select {:variant   :standard
                   :value     replace-using-specs?
                   :on-change #(>evt :randomizer/set-replace-using-specs? (event/value %))}
           [menu-item {:value true} "card matching rule, if possible"]
           [menu-item {:value false} "any card"]]]])]]))

(defn market-display []
  (let [market       (<sub :randomizer/market)
        specs        (<sub :randomizer/specs)
        filter-utils (<sub :filter-utils)
        edit?        (<sub :randomizer/edit?)
        show-specs?  (<sub :randomizer/show-specs?)]
    [card-list
     {:tooltip          "Replace card"
      :href-fn          false
      :on-click-fn      (if edit?
                          ;; Not if display is sorted. Replacing cards in this mode will more often than not result
                          ;; in a reordering of the cards (because a card is replaced by a different cost) which
                          ;; makes it seem like multiple cards are replaced. Also, it makes it less transparent why
                          ;; a certain card is selected.
                          ;; The alternative would be to make an option that shows the spec name under the card and
                          ;; a sort button. I'm not sure that would make it more user-friendly.
                          (fn [card]
                            #(>evt :randomizer/replace-card filter-utils specs (:spec-idx card))))
      :content-below-fn (if show-specs?
                          (fn [card]
                            (:spec-name card)))
      :card-image-sx-fn (fn [card]
                          (if (:unresolved? card)
                            {:background-color "rgb(255, 0, 0)"
                             :opacity          0.4}))}
     market]))

(defn randomizer-page []
  (page (txt :randomizer/page-title)
    [generate-market-button]
    (if (<sub :randomizer/market-generated?)
      [:<>
       [market-toolbar]
       [unresolved-alert]
       [market-display]])))
