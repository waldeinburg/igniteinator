(ns igniteinator.randomizer.randomizer-test
  (:require [clojure.test :refer [are deftest is]]
            [igniteinator.randomizer.card-specs :refer [card-specs]]
            [igniteinator.randomizer.randomizer :refer [generate-market get-randomizer-cards get-title-cards]]))

(deftest base-filters-selects-correct
  (let [filter-utils {:march-id 1, :dagger-id 2, :old-wooden-shield-id 3,
                      :title?   #(>= (:id %) 6)}
        cards        (mapv (fn [id] {:id id}) (range 1 9))]
    (is (= [4 5] (mapv :id (get-randomizer-cards filter-utils cards))))
    (is (= [6 7 8] (mapv :id (get-title-cards filter-utils cards))))))

(deftest generate-market-from-base-specs
  (let [filter-utils {:title?           #(> (:id %) 100)
                      :movement?        #(= :type :mov)
                      :provides-damage? #(= :provides-effect :dmg)}
        specs        (card-specs filter-utils)]
    (are [cards market-ids]
      (= market-ids (mapv :id (generate-market filter-utils cards specs)))
      ;; Simple, no combos
      [{:id 1, :type :mov, :cost 4}
       {:id 2, :type :mov, :cost 6}
       {:id 3, :provides-effect :dmg, :cost 5}
       {:id 4, :provides-effect :dmg, :cost 7}
       {:id 5, :provides-effect :dmg, :cost 8}
       {:id 6, :provides-effect :dmg, :cost 9}
       {:id 7, :cost 3}
       {:id 8, :cost 10}
       {:id 9}
       {:id 10}
       {:id 11}
       {:id 12}
       {:id 13}
       {:id 14}
       {:id 15}
       {:id 16}
       {:id 17}
       {:id 101}
       {:id 102}
       {:id 103}]
      (into (vec (range 1 17)) [101 102]))))
