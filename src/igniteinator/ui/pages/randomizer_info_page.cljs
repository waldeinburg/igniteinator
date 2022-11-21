(ns igniteinator.ui.pages.randomizer-info-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.link :refer [internal-link]]
            [igniteinator.ui.components.page :refer [page]]
            [reagent-mui.material.button :refer [button]]))

(defn randomizer-info-page []
  (page (txt :randomizer/info-page-title)
    [back-button {:variant :contained}]
    [internal-link :randomizer/data
     {:navigate-event :page/to-other-sub-page
      :component      button
      :sx             {:ml 2}
      :variant        :outlined}
     "metadata"]
    [:p "The randomizer aims to follow the recommendations on page 21 of the rule book and take advantage of the
    registered known combos to create a market that works well. It will pick 16 cards based on a rule for each cards,
    plus 2 random title cards."]
    [:p "The rule book recommends having a market with:"]
    [:ul
     [:li "Cards costing a variety of amounts"]
     [:li "At least 2 movement cards"]
     [:li "At least 4 cards which produce damage."]
     [:li "Prerequisite cards (for example, bows if the market contains arrows and vice versa)"]]
    [:p "The randomizer will shuffle all cards from the selected boxes (in the options) and select the first card
    matching each rule:"]
    [:ol
     [:li [:strong "Movement card costing 4-6:"] " Ensure one cheap movement card (4-6 being to make a decent amount of
     cards to select among)"]
     [:li [:strong "Movement card of a different cost"] " (than the previous one)"]
     [:li [:strong "Card providing damage costing 4-5:"] " Ensure one cheap damage producing card (4-5 being enough to
     make a decent amount of cards to select among)"]
     [:li [:strong "Card providing damage of a different cost"] " (than the damage card, not counting they movement
     cards)"]
     [:li [:strong "Card providing damage of a different cost"]]
     [:li [:strong "Card providing damage of a different cost"]]
     [:li [:strong "Ensure cards of cost 3-10"] ", i.e., look at what costs have already been selected and select a
     different cost. 11 is not included; there's only two cards from the base game plus Dragon Potion, and the
     randomizer should not always include those few cards."]
     [:li [:strong "Ensure cards of cost 3-10"]]
     [:li [:strong "Ensure cards of cost 3-10 or choose a random card if already satisfied"] " (if the cards have no
     duplicate costs between rule 1-2 and 3-6)"]
     [:li [:strong "Ensure cards of cost 3-10 or choose a random card if already satisfied"] " (if the cards have one or
     no duplicate costs between rule 1-2 and 3-6)"]
     [:li [:strong "One more expensive (cool) cards (cost 10-11)"] ": This assumes that a few expensive cards (and more than one) makes for a more exciting game, and it decreases the probability that cards costing 11 extremely rarely show up."]
     [:li [:strong "Combo for other cards or random if none"]]
     [:li [:strong "Combo for other cards or random if none"]]
     [:li [:strong "Combo for other cards or random if none"]]
     [:li [:strong "Random card:"] " Registered combos are limited and we want all cards to have decent chance of being
     selected"]
     [:li [:strong "Random card"]]]
    [:p "The randomizer will then go through the market and replace cards from the back (i.e., card 16, 15 and so forth)
    if any requirements are not satisfied (e.g., arrows but no bows). It will follow the rule of the card if
    possible."]))
