(ns igniteinator.model.epic-setups
  (:require [igniteinator.ui.components.link :refer [external-link]]))

;; Nasty hacks to avoid being dependent on the database for the filters.
(def types [:0 :title :ability :aoe :blade :bow :equipment :event :fire :freeze :healing :immovable :impassable-terrain
            :item :lightning :melee :minion :mount :movement :nature :passable-terrain :projectile :ranged :shield
            :small :special :spell :terrain :weapon :dark :fly :modifier :monetary :war-machine :transformation])
(def old-wooden-shield-id 75)

(defn type-id [t]
  (.indexOf types t))

(defn type= [type card]
  (contains? (:types card) (type-id type)))

(defn cost= [cost card]
  (= cost (:cost card)))

(def epic-setups
  [{:name        "Epic Ignite"
    :description "As described in the Ignite rule book page ?????."
    :stacks      []}
   {:name        "Even More Epic Ignite"
    :description [:<> "Variant described on "
                  [external-link "https://boardgamegeek.com/thread/2767913/even-more-epic-ignite" "BGG"]
                  "."]
    :stacks      [{:name        "Actions A"
                   :description "All cards of type Ability or Event and costing 3"
                   :filter      #(and
                                   (or (type= :ability %) (type= :event %))
                                   (cost= % 3))}
                  {:name        "Actions B"
                   :description "All cards of type Ability or Event and costing 4 or 5"
                   :filter      #(and
                                   (or (type= :ability %) (type= :event %))
                                   (or (cost= 4 %) (cost= 5 %)))}
                  {:name        "Actions B"
                   :description "All cards of type Ability or Event and costing 6 or 7"
                   :filter      #(and
                                   (or (type= :ability %) (type= :event %))
                                   (or (cost= 6 %) (cost= 7 %)))}
                  {:name        "Rare Objects"
                   :description "All cards of type Shield or Bow or War Machine except of Old Wooden Shield"
                   :filter      #(and
                                   (not= old-wooden-shield-id (:id %))
                                   (or (type= :shield %) (type= :war-machine %)))}
                  {:name        "Spells A"
                   :description "All cards of type Spell and Costing 4 or 5 except of type Projectile"
                   :filter      #(and
                                   (type= :spell %)
                                   (or (cost= 4 %) (cost= 5 %))
                                   (not (type= :projectile %)))}]}])
