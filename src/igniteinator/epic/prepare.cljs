(ns igniteinator.epic.prepare
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.material.button :refer [button]]
            [igniteinator.ui.components.link :refer [external-link]]
            [reagent-mui.material.alert :refer [alert]]))

(defn shuffle-button []
  (if (not (<sub :epic/active?))
    (let [cards (<sub :global-cards-unsorted)]
      [button {:on-click #(>evt :epic/create-game cards 1)
               :variant  :contained
               :sx       {:mb 2}}
       "Shuffle"])))

(defn prepare-game []
  (if (not (<sub :epic/active?))
    (let [epic-setups (<sub :epic/setups)]
      [:<>
       [alert {:severity :info
               :sx       {:mb 2}}
        [:p
         "This is a beta version to get community feedback on the interaction with the cards. Currently only "
         [external-link "https://boardgamegeek.com/thread/2767913/even-more-epic-ignite" (:name (epic-setups 1))]
         " is implemented."]
        [:p "To be implemented:"]
        [:ul
         [:li "Choose between Even More Epic Ignite and Epic Ignite as described in the Ignite rule book page 23."]
         [:li "Show last action + undo/redo."]
         [:li "Take actions on the card display page (when clicking a card)"]]]
       [shuffle-button]
       [:h3 "How to play"]
       [:ul
        [:li "The app will display the top card of each stack in the market. Take these from the box and place them "
         "on the table."]
        [:li "Tell the app when you are taking a card from the market, cycling a card to the bottom of the stack or "]
        "trashing a card to the bottom of the stack (Even More Epic Ignite only)."
        [:li "When a new top card is displayed, take the new card from the box and put it on the appropriate stack."]
        [:li "When a card is sent to the bottom of a stack, place it in the stack for convenience. If it shows up as "
         "the top card, just use that one instead of taking a new from the box."]]])))
