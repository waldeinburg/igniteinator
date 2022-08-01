(ns igniteinator.model.epic-setups
  (:require [igniteinator.ui.components.link :refer [external-link]]))

(defn- find-id-by-name-fn [coll]
  (fn [name]
    (some #(if (= name (:name %))
             (:id %))
      coll)))

(defn epic-setups [cards types has-the-freeze? has-a-new-enemy?]
  "Get list of Epic setup types.
  To be called from a subscription."
  (let [split-50-50-stacks   (fn [name]
                               [{:name    (str name " A")
                                 :take-fn (fn [cards]
                                            (let [half (js/Math.ceil (/ (count cards) 2))]
                                              (partition half half nil cards)))}
                                {:name    (str name " B")
                                 :take-fn #(list % nil)}])
        find-card-id-by-name (find-id-by-name-fn cards)
        find-type-id-by-name (find-id-by-name-fn types)
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
        cost<=               (fn [cost card]
                               (>= cost (:cost card)))
        cost>=               (fn [cost card]
                               (<= cost (:cost card)))
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
        item?                (is-type-fn "Item")
        title?               (is-type-fn "Title")]
    [{:name             "Epic Ignite"
      :description
      (if has-the-freeze?
        "As described in the Ignite rule book page 23 and The Freeze rule book page 9."
        "As described in the Ignite rule book page 23.")
      :trash-to-bottom? false
      :count-fn         #(:count %)
      :stacks
      (if has-the-freeze?
        [{:split?      true
          :description "Weapon and Shield cards costing 5 and below"
          :filter      #(and
                          (or
                            (weapon? %)
                            (shield? %))
                          (not= dagger-id (:id %))
                          (not= old-wooden-shield-id (:id %))
                          (cost<= 5 %))
          :sub-stacks  (split-50-50-stacks "Cheap arms")}
         {:split?      true
          :description "Weapon and Shield cards costing 6 and above"
          :filter      #(and
                          (or
                            (weapon? %)
                            (shield? %))
                          (cost>= 6 %))
          :sub-stacks  (split-50-50-stacks "Expensive arms")}
         {:split?      true
          :description "Event and Ability cards costing 5 and below"
          :filter      #(and
                          (or
                            (event? %)
                            (ability? %))
                          (cost<= 5 %))
          :sub-stacks  (split-50-50-stacks "Cheap aids")}
         {:split?      true
          :description "Event and Ability cards costing 6 and above"
          :filter      #(and
                          (or
                            (event? %)
                            (ability? %))
                          (cost>= 6 %))
          :sub-stacks  (split-50-50-stacks "Expensive aids")}
         {:split?      true
          :description "Pure Spell cards costing 7 and below"
          :filter      #(and
                          (spell? %)
                          (cost<= 7 %))
          :sub-stacks  (split-50-50-stacks "Cheap spells")}
         {:split?      true
          :description "Pure Spell cards costing 8 and above"
          :filter      #(and
                          (spell? %)
                          (cost>= 8 %))
          :sub-stacks  (split-50-50-stacks "Expensive spells")}
         ;; Assuming an error in The Freeze rule book which has one stack for each of the following. However:
         ;; - The base game rules has two Movement stacks of 45 cards instead of one big 90 cards stack.
         ;; - The largest stacks are otherwise the Expensive Aids stacks with 80 cards each (still a large number).
         ;; - The Items and War Machine stack would have 170 cards without splitting.
         ;; - The Projectiles stack would have 100 cards without splitting.
         {:split?      true
          :description "Item and War Machine cards"
          :filter      #(or
                          (item? %)
                          (war-machine? %))
          :sub-stacks  (split-50-50-stacks "Items")}
         {:split?      true
          :description "Projectile cards"
          :filter      #(projectile? %)
          :sub-stacks  (split-50-50-stacks "Projectiles")}
         {:split?      true
          :description "Movement cards"
          :filter      #(and
                          (movement? %)
                          (not= march-id (:id %)))
          :sub-stacks  (split-50-50-stacks "Movement")}]
        ;; Base game only
        [{:split?      true
          :description "Weapon, Shield, and Projectile cards costing 5 and below"
          :filter      #(and
                          (or
                            (weapon? %)
                            (shield? %)
                            (projectile? %))
                          (not= dagger-id (:id %))
                          (not= old-wooden-shield-id (:id %))
                          (cost<= 5 %))
          :sub-stacks  (split-50-50-stacks "Cheap arms")}
         {:split?      true
          :description "Weapon, Shield, and Projectile cards costing 6 and above"
          :filter      #(and
                          (or
                            (weapon? %)
                            (shield? %)
                            (projectile? %))
                          (cost>= 6 %))
          :sub-stacks  (split-50-50-stacks "Expensive arms")}
         {:split?      true
          :description "Item, Event, and Ability cards costing 4 and below"
          :filter      #(and
                          (or
                            (item? %)
                            (event? %)
                            (ability? %))
                          (cost<= 4 %))
          :sub-stacks  (split-50-50-stacks "Cheap aids")}
         {:split?      true
          :description "Item, Event, and Ability cards costing 5 and above"
          :filter      #(and
                          (or
                            (item? %)
                            (event? %)
                            (ability? %))
                          (cost>= 5 %))
          :sub-stacks  (split-50-50-stacks "Expensive aids")}
         {:split?      true
          :description "Pure Spell cards costing 7 and below"
          :filter      #(and
                          (spell? %)
                          (cost<= 7 %))
          :sub-stacks  (split-50-50-stacks "Cheap spells")}
         {:split?      true
          :description "Pure Spell cards costing 8 and above"
          :filter      #(and
                          (spell? %)
                          (cost>= 8 %))
          :sub-stacks  (split-50-50-stacks "Expensive spells")}
         {:split?      true
          :description "Movement cards"
          :filter      #(and
                          (movement? %)
                          (not= march-id (:id %)))
          :sub-stacks  (split-50-50-stacks "Movement")}])}
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
                          :description "Movement cards, except of March"
                          :filter      #(and
                                          (not= march-id (:id %))
                                          (movement? %))}
                         {:name        "Actions A"
                          :description "Ability and Event cards costing 3"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost= 3 %))}
                         {:name        "Actions B"
                          :description "Ability and Event cards costing 4 or 5"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost>=< 4 5 %))}
                         {:name        "Actions C"
                          :description "Ability and Event cards costing 6 or 7"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost>=< 6 7 %))}
                         {:name        "Actions D"
                          :description "Ability and Event cards costing from 8 to 11"
                          :filter      #(and
                                          (or (ability? %) (event? %))
                                          (cost>=< 8 11 %))}
                         {:name        "Weapons A"
                          :description "Weapon cards, except of type Bow, costing from 4 to 6"
                          :filter      #(and
                                          (weapon? %)
                                          (not (bow? %))
                                          (cost>=< 4 6 %))}
                         {:name        "Weapons B"
                          :description "Weapon cards, except of type Bow, costing from 7 to 9"
                          :filter      #(and
                                          (weapon? %)
                                          (not (bow? %))
                                          (cost>=< 7 9 %))}
                         {:name        "Rare Objects"
                          :description "Shield, Bow, and War Machine cards, except of Old Wooden Shield"
                          :filter      #(and
                                          (not= old-wooden-shield-id (:id %))
                                          (or (shield? %) (bow? %) (war-machine? %)))}
                         {:name        "Projectiles"
                          :description "Projectile cards"
                          :filter      #(projectile? %)}
                         {:name        "Spells A"
                          :description "Pure Spell cards costing 4 or 5"
                          :filter      #(and
                                          (spell? %)
                                          (cost>=< 4 5 %))}
                         {:name        "Spells B"
                          :description "Pure Spell cards costing 6 or 7"
                          :filter      #(and
                                          (spell? %)
                                          (cost>=< 6 7 %))}
                         {:name        "Spells C"
                          :description "Pure Spell cards costing 8 or 9"
                          :filter      #(and
                                          (spell? %)
                                          (cost>=< 8 9 %))}
                         {:name        "Spells D"
                          :description (str "Pure Spell cards costing 10 or 11"
                                         (if has-a-new-enemy?
                                           ", and Dragon Potion"))
                          :filter      #(or
                                          (and
                                            (spell? %)
                                            (or (cost= 10 %) (cost= 11 %)))
                                          (= dragon-potion-id (:id %)))}
                         {:name        "Items A"
                          :description "Item cards costing from 3 to 5"
                          :filter      #(and
                                          (item? %)
                                          (cost>=< 3 5 %))}
                         {:name        "Items B"
                          :description "Item cards costing from 6 to 9"
                          :filter      #(and
                                          (item? %)
                                          (cost>=< 6 9 %))}
                         ;; Place the titles last so that they can be ignored if unwanted.
                         {:split?     true
                          :filter     #(title? %)
                          :sub-stacks [{:name         "Title A"
                                        :description  "First of two title cards"
                                        :placeholder? true
                                        :take-fn      #(list [(first %)] (rest %))}
                                       {:name         "Title B"
                                        :description  "Second of two title cards"
                                        :placeholder? true
                                        :take-fn      #(list [(first %)] nil)}]}]}]))
