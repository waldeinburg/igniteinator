(ns igniteinator.epic.prepare
  (:require [igniteinator.ui.components.radio-group :refer [radio radio-group]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.material.alert :refer [alert]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.form-helper-text :refer [form-helper-text]]
            [reagent.core :as r]))

(defn shuffle-button []
  (if (not (<sub :epic/active?))
    (let [cards (<sub :global-cards-unsorted)
          setup (<sub :epic/setup)]
      [button {:on-click #(>evt :epic/create-game cards setup)
               :variant  :contained
               :sx       {:mb 2}}
       "Shuffle"])))

(defn select-variant-radios []
  (let [epic-setups (<sub :epic/setups)]
    [radio-group {:label      "Choose variant"
                  :value-ref  (<sub-ref :epic/setup-idx)
                  :value-type :number
                  :on-change  #(>evt :epic/set-setup-idx %)}
     (doall
       (map-indexed
         (fn [idx setup]
           ^{:key idx}
           [radio {:value idx
                   :label (r/as-element [:<> (:name setup)
                                         [form-helper-text (:description setup)]])}])
         epic-setups))]))

(defn prepare-game []
  [:<>
   [alert {:severity :info
           :sx       {:mb 2}}
    [:p "This is a beta version to get community feedback on the interaction with the cards."]
    [:p "To be implemented:"]
    [:ul
     [:li "Undo/redo."]]]
   [:p "This feature lets you play Epic Ignite without sorting all of your cards before and after the game."]
   [select-variant-radios]
   [box {:mt 2} [shuffle-button]]
   [:h3 "How to play"]
   [:ul
    [:li "The app will display the top card of each randomized stack in the market. Take these from the box and "
     "place them on the table instead of full stacks."]
    [:li "Stacks with just March, Dagger, or Old Wooden Shield are left out because app interaction is not "
     "necessary for these stacks. However, Title stacks are included at the bottom with buttons disabled to "
     "include randomization of Title cards."]
    [:li "Tell the app when you are taking a card from the market, cycling a card to the bottom of the stack or "]
    "trashing a card to the bottom of the stack (Even More Epic Ignite only)."
    [:li "When a new top card is displayed, take the new card from the box and put it on the appropriate stack."]
    [:li "When a card is sent to the bottom of a stack, place it in the stack for convenience. If it shows up as "
     "the top card, just use that one instead of taking a new from the box."]]])
