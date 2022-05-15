(ns igniteinator.model.epic-setups
  (:require [igniteinator.ui.components.link :refer [external-link]]))

(defn epic-setups [cards types]
  "Get list of Epic setup types.
  To be called from a subscription."
  (let [type-fn              (fn [t]
                               (:id (some #(= t (:name %)) types)))
        cost=                (fn [cost card]
                               (= cost (:cost card)))
        old-wooden-shield-id (:id (some #(= "Old Wooden Shield" (:name %)) cards))
        ability?             (type-fn "Ability")
        event?               (type-fn "Event")
        shield?              (type-fn "Shield")
        war-machine?         (type-fn "War Machine")
        spell?               (type-fn "Spell")
        projectile?          (type-fn "Projectile")]
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
                                     (or (ability? %) (event? %))
                                     (cost= % 3))}
                    {:name        "Actions B"
                     :description "All cards of type Ability or Event and costing 4 or 5"
                     :filter      #(and
                                     (or (ability? %) (event? %))
                                     (or (cost= 4 %) (cost= 5 %)))}
                    {:name        "Actions B"
                     :description "All cards of type Ability or Event and costing 6 or 7"
                     :filter      #(and
                                     (or (ability? %) (event? %))
                                     (or (cost= 6 %) (cost= 7 %)))}
                    {:name        "Rare Objects"
                     :description "All cards of type Shield or Bow or War Machine except of Old Wooden Shield"
                     :filter      #(and
                                     (not= old-wooden-shield-id (:id %))
                                     (or (shield? %) (war-machine? %)))}
                    {:name        "Spells A"
                     :description "All cards of type Spell and Costing 4 or 5 except of type Projectile"
                     :filter      #(and
                                     (spell? %)
                                     (or (cost= 4 %) (cost= 5 %))
                                     (not (projectile? %)))}]}]))
