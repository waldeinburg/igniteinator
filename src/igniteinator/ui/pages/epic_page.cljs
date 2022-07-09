(ns igniteinator.ui.pages.epic-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.components.link :refer [external-link]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [reagent-mui.material.alert :refer [alert]]
            [reagent-mui.material.button :refer [button]]))

(defn stacks-display []
  (let [top-cards (<sub :epic/top-cards)]
    (if (not-empty top-cards)
      [card-list top-cards])))

(defn shuffle-button []
  (let [cards (<sub :global-cards-unsorted)]
    [button {:on-click #(>evt :epic/create-game cards 1)
             :variant  :contained
             :sx       {:mb 2}}
     "Shuffle"]))

(defn epic-page []
  (let [epic-setups (<sub :epic/setups)]
    [page (txt :epic-page-title)
     [alert {:severity :info
             :sx       {:mb 2}}
      "This is a beta version to get community feedback on the interaction with the cards. Currently only "
      [external-link "https://boardgamegeek.com/thread/2767913/even-more-epic-ignite" (:name (epic-setups 1))]
      " is implemented; in the final version you can choose " (:name (epic-setups 0))
      " as described in the Ignite rule book page 23."]
     [shuffle-button]
     [stacks-display]]))
