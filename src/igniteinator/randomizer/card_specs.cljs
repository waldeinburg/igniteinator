(ns igniteinator.randomizer.card-specs
  (:require [clojure.set :refer [difference]]
            [igniteinator.util.filter :refer [cost= cost>= cost>=<]]))

(defn card-specs [{:keys [movement? provides-damage?]}]
  (let [basic-costs                 (set (range 3 11))
        high-cost                   10
        damage-card-other-cost      (fn [selected-cards]
                                      (let [other-damage-cards-cost (->>
                                                                      (nthrest selected-cards 2)
                                                                      (map :cost)
                                                                      set)]
                                        #(and
                                           (provides-damage? %)
                                           (not (other-damage-cards-cost (:cost %))))))
        ensure-basic-cost-or-random (fn [selected-cards]
                                      (let [other-costs (set (map :cost selected-cards))
                                            costs       (difference basic-costs other-costs)]
                                        (if (empty? costs)
                                          (constantly true)
                                          #(costs (:cost %)))))
        is-combo                    (fn [selected-cards]
                                      (let [combo-ids (->> selected-cards
                                                        (mapcat :combos)
                                                        set)]
                                        #(combo-ids (:id %))))
        any                         (fn [_]
                                      (constantly true))]
    [{:name   "Movement card costing 4-6"
      :filter (fn [_]
                #(and
                   (movement? %)
                   (cost>=< 4 6 %)))}
     {:name   "Movement card of a different cost"
      :filter (fn [selected-cards]
                (let [other-movement-card-cost (:cost (first selected-cards))]
                  #(and
                     (movement? %)
                     (not (cost= other-movement-card-cost %)))))}
     {:name   "Card providing damage cosing 4-5"
      :filter (fn [_]
                #(and
                   (provides-damage? %)
                   (cost>=< 4 5 %)))}
     {:name   "Card providing damage of a different cost"
      :filter damage-card-other-cost}
     {:name   "Card providing damage of a different cost"
      :filter damage-card-other-cost}
     {:name   "Card providing damage of a different cost"
      :filter damage-card-other-cost}
     ;; 3-10 is 8 different costs. Worst case is that we have only selected 4 different cost by now (if two of the four
     ;; damage cards has the same cost as the two movement cards). Best case is 6 different costs. Thus, we need 2-4
     ;; more filters to ensure cost 3-10.
     ;; 11 not included; there's only two cards from the base game plus Dragon Potion and the randomizer should not
     ;; always include those few.
     {:name   "Ensure cards of cost 3-10"
      :filter ensure-basic-cost-or-random}
     {:name   "Ensure cards of cost 3-10"
      :filter ensure-basic-cost-or-random}
     ;; Now we may or may not have all wanted costs
     {:name   "Ensure cards of cost 3-10 or choose a random card if already satisfied"
      :filter ensure-basic-cost-or-random}
     {:name   "Ensure cards of cost 3-10 or choose a random card if already satisfied"
      :filter ensure-basic-cost-or-random}
     {:name   "Ensure more expensive (cool) cards (cost 10-11)"
      :filter (fn [_]
                #(cost>= high-cost %))}
     {:name   "Combo for other cards or random if none"
      :filter is-combo}
     {:name   "Combo for other cards or random if none"
      :filter is-combo}
     {:name   "Combo for other cards or random if none"
      :filter is-combo}
     {:name   "Random card"
      :filter any}
     {:name   "Random card"
      :filter any}]))
