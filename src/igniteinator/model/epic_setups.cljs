(ns igniteinator.model.epic-setups
  (:require [igniteinator.ui.components.link :refer [external-link]]))

(defn epic-setups [cards types]
  "Get list of Epic setup types.
  To be called from a subscription."
  (let [find-card-id-by-name (fn [name]
                               (some #(if (= name (:name %))
                                        (:id %))
                                 cards))
        find-type-id-by-name (fn [type-name]
                               (some
                                 #(if (= type-name (:name %))
                                    (:id %))
                                 types))
        is-type-fn           (fn [type-name]
                               (let [type-id (find-type-id-by-name type-name)]
                                 (fn [card]
                                   (= type-id (-> card :types (get 0))))))
        has-type-fn          (fn [type-name]
                               (let [type-id (find-type-id-by-name type-name)]
                                 (fn [card]
                                   (some #(= type-id %) (:types card)))))
        cost=                (fn [cost card]
                               (= cost (:cost card)))
        cost>=<              (fn [cost-from cost-to card]
                               (<= cost-from (:cost card) cost-to))
        march-id             (find-card-id-by-name "March")
        dagger-id            (find-card-id-by-name "Dagger")
        old-wooden-shield-id (find-card-id-by-name "Old Wooden Shield")
        dragon-potion-id     (find-card-id-by-name "Dragon Potion")
        bow?                 (has-type-fn "Bow")
        movement?            (is-type-fn "Movement")
        ability?             (is-type-fn "Ability")
        event?               (is-type-fn "Event")
        weapon?              (is-type-fn "Weapon")
        shield?              (is-type-fn "Shield")
        war-machine?         (is-type-fn "War Machine")
        projectile?          (is-type-fn "Projectile")
        spell?               (is-type-fn "Spell")
        item?                (is-type-fn "Item")]
    ;; TODO: Allow stack groups to allow normal Epic Ignite and title cards in Even More Epic Ignite.
    [{:name             "Epic Ignite"
      :description      "As described in the Ignite rule book page 23."
      :trash-to-bottom? false
      :count-fn         #(:count %)
      :stacks           []
      :stacks-process   (fn [stacks]
                          ;; Split all stacks into two.
                          (mapcat (fn [stack]
                                    (let [cards   (:cards stack)
                                          half    (/ (count cards) 2)
                                          cards-a (take (js/Math.ceil half) cards)
                                          cards-b (take (js/Math.floor half) cards)
                                          stack-a (assoc stack :cards cards-a)
                                          stack-b (assoc stack :cards cards-b)]
                                      [stack-a stack-b]))
                            stacks))}
     {:name             "Even More Epic Ignite"
      :description      [:<> "Variant described on "
                         [external-link "https://boardgamegeek.com/thread/2767913/even-more-epic-ignite" "BGG"]
                         "."]
      :trash-to-bottom? true
      :count-fn         (fn [card]
                          (let [c (:count card)]
                            (if (#{march-id dagger-id old-wooden-shield-id} (:id card))
                              c
                              (/ c 2))))
      :stacks           [{:name        "Movements"
                          :description "All cards of type Movement except of title March"
                          :filter      #(and
                                          (not= march-id (:id %))
                                          (movement? %))}
                         {:name        "Actions A"
                          :description "All cards of type Ability or Event and costing 3"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost= 3 %))}
                         {:name        "Actions B"
                          :description "All cards of type Ability or Event and costing 4 or 5"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost>=< 4 5 %))}
                         {:name        "Actions C"
                          :description "All cards of type Ability or Event and costing 6 or 7"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost>=< 6 7 %))}
                         {:name        "Actions D"
                          :description "All cards of type Ability or Event and costing from 8 to 11"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost>=< 8 11 %))}
                         {:name        "Weapons A"
                          :description "All cards of type Weapon except of type Bow and costing from 4 to 6"
                          :filter      #(and
                                          (weapon? %)
                                          (not (bow? %))
                                          (cost>=< 4 6 %))}
                         {:name        "Weapons B"
                          :description "All cards of type Weapon except of type Bow and costing from 7 to 9"
                          :filter      #(and
                                          (weapon? %)
                                          (not (bow? %))
                                          (cost>=< 7 9 %))}
                         {:name        "Rare Objects"
                          :description "All cards of type Shield or Bow or War Machine except of title Old Wooden Shield"
                          :filter      #(and
                                          (not= old-wooden-shield-id (:id %))
                                          (or (shield? %) (bow? %) (war-machine? %)))}
                         {:name        "Projectiles"
                          :description "All cards of type Projectile"
                          :filter      #(projectile? %)}
                         {:name        "Spells A"
                          :description "All cards of type Spell and Costing 4 or 5 except of type Projectile"
                          :filter      #(and
                                          (spell? %)
                                          (cost>=< 4 5 %)
                                          (not (projectile? %)))}
                         {:name        "Spells B"
                          :description "All cards of type Spell and Costing 6 or 7 except of type Projectile"
                          :filter      #(and
                                          (spell? %)
                                          (cost>=< 6 7 %)
                                          (not (projectile? %)))}
                         {:name        "Spells C"
                          :description "All cards of type Spell and Costing 8 or 9"
                          :filter      #(and
                                          (spell? %)
                                          (cost>=< 8 9 %))}
                         {:name        "Spells D"
                          :description "All cards of type Spell and costing 10 or 11 (and Dragon Potion if applicable)"
                          :filter      #(or
                                          (and
                                            (spell? %)
                                            (or (cost= 10 %) (cost= 11 %)))
                                          (= dragon-potion-id (:id %)))}
                         {:name        "Items A"
                          :description "All cards of type Item and costing from 3 to 5"
                          :filter      #(and
                                          (item? %)
                                          (cost>=< 3 5 %))}
                         {:name        "Items B"
                          :description "All cards of type Item and costing from 6 to 9"
                          :filter      #(and
                                          (item? %)
                                          (cost>=< 6 9 %))}]}]))
