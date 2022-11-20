(ns igniteinator.ui.pages.randomizer-data-page
  (:require [clojure.string :as s]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref]]
            [reagent-mui.material.box :refer [box]]))

(defn value-list [ids val-map]
  (s/join ", " (mapv #(:name (val-map %)) ids)))

(defn entry [title tooltip-str val-map ids]
  [box [tooltip tooltip-str [:strong title ":"]] " " (value-list ids val-map)])

(defn content-below [types-map effects-map card]
  [:<>
   (if-let [effect-ids (:provides-effect card)]
     [entry "Provides effect"
      "This card provides the following effects"
      effects-map
      effect-ids])
   (if-let [effect-ids (:requires-effect card)]
     [entry "Requires effect"
      "Setup must have one card providing one of the following effects"
      effects-map
      effect-ids])
   (if-let [type-ids (:requires-type card)]
     [entry "Requires type"
      "Setup must have one card of one of the following types"
      types-map
      type-ids])
   (if-let [type-ids (:requires-type-except card)]
     [entry "Requires type exceptions"
      "Cards with the following types does not satisfy the Requires type condition above"
      types-map
      type-ids])])

(defn randomizer-data-page []
  (let [cards       (<sub :randomizer/all-cards)
        types-map   (<sub :types-map)
        effects-map (<sub :effects-map)]
    [page (txt :randomizer/metadata-page-title)
     [back-button]
     [:p
      "Multiple requirements means that one of the types/effects in the list should be in the market, not necessarily "
      "all."]
     [:p
      "Please notice that a trait is only added to a card if it makes sense, not just because it's formally true. "
      "E.g., Unicorn can knock down a unit if blocked by a shield. However, this is not enough to satisfy requirements "
      "for Warhammer: Without any shields in the setup Unicorn can only knock down while there's still Old Wooden "
      "Shield cards left in the decks, making Warhammer close to worthless. "
      "Similarly, as the randomizer should always add cards providing Damage, no cards have Damage set as a required "
      "effect."]
     [card-list
      {:on-click         false
       :tooltip          false
       :content-below-fn (partial content-below types-map effects-map)}
      cards]]))
