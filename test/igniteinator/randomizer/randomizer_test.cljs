(ns igniteinator.randomizer.randomizer-test
  (:require [clojure.test :refer [are deftest is]]
            [igniteinator.randomizer.card-specs :refer [card-specs]]
            [igniteinator.randomizer.randomizer :refer [generate-market get-randomizer-cards get-title-cards]]))

(defn card-range [start end]
  (map (fn [id] {:id id}) (range start end)))

(defn id-range [start end]
  (range start end))

(deftest base-filters-selects-correct
  (let [filter-utils {:march-id 1, :dagger-id 2, :old-wooden-shield-id 3,
                      :title?   #(>= (:id %) 6)}
        cards        (card-range 1 9)]
    (is (= [4 5] (mapv :id (get-randomizer-cards filter-utils cards))))
    (is (= [6 7 8] (mapv :id (get-title-cards filter-utils cards))))))

(deftest generate-market-from-base-specs
  (let [filter-utils {:title?           #(> (:id %) 100)
                      :movement?        #(= :types [:mov])
                      :provides-damage? #(= :provides-effect [:dmg])}
        specs        (card-specs filter-utils)]
    (are [cards market-ids]
      (= market-ids (mapv :id (generate-market filter-utils cards specs)))
      ;; No combos, no dependencies, select cost 11 at cost 10-11 rule.
      (concat
        [{:id 1, :types [:mov], :cost 4}
         {:id 2, :types [:mov], :cost 6}
         {:id 3, :provides-effect [:dmg], :cost 5}
         {:id 4, :provides-effect [:dmg], :cost 7}
         {:id 5, :provides-effect [:dmg], :cost 8}
         {:id 6, :provides-effect [:dmg], :cost 9}
         {:id 7, :cost 3}
         {:id 8, :cost 10}
         {:id 9}
         {:id 10}
         ;; 12 will be selected before 11 because it satisfies cost 10-11.
         {:id 11, :cost 9}
         {:id 12, :cost 11}]
        (card-range 13 20)
        (card-range 101 105))
      (concat (id-range 1 11) [12 11] (id-range 13 17) [101 102])
      ;; No dependencies, select combos.
      (concat
        [{:id 1, :types [:mov], :cost 4, :combos [28, 29]}
         {:id 2, :types [:mov], :cost 6}
         {:id 3, :provides-effect [:dmg], :cost 5, :combos [25]}
         {:id 4, :provides-effect [:dmg], :cost 7, :combos [21]}
         {:id 5, :provides-effect [:dmg], :cost 8}
         {:id 6, :provides-effect [:dmg], :cost 9}
         {:id 7, :cost 3}
         {:id 8, :cost 10}]
        (card-range 9 30)
        (card-range 101 105))
      (concat (range 1 12) [21 25 28 12 13 101 102])
      ;; Simple requirements, just overwrite cards.
      (concat
        [{:id 1, :types [:mov], :cost 4, :requires-effect [:foo]}
         {:id 2, :types [:mov], :cost 6}
         {:id 3, :types [:goo] :provides-effect [:dmg], :cost 5, :requires-type [:goo]}
         {:id 4, :provides-effect [:dmg], :cost 7, :requires-type [:bar], :requires-type-except [:baz]}
         {:id 5, :provides-effect [:dmg], :cost 8, :requires-effect [:quz :qoo]}
         {:id 6, :provides-effect [:dmg], :cost 9, :requires-type [:qux :quy]}
         {:id 7, :cost 3}
         {:id 8, :cost 10}]
        (card-range 9 20)
        [{:id 20, :provides-effect [:mov]}                  ; Not type
         {:id 21, :types [:foo]}                            ; Not effect
         {:id 22, :types [:bar :baz]}                       ; Not selected because of except
         {:id 23, :types [:bar :quy]}                       ; Satifisfies two
         {:id 24, :provides-effect [:foo]}
         {:id 25, :provides-effect [:qru :qoo]}             ; Satisfies, but not with primary type
         {:id 26, :types [:quy]}                            ; Already satisfied.
         {:id 27, :types [:hoo :goo]}]
        (card-range 28 30)
        (card-range 101 105))
      (concat (range 1 13) [25 23 27 24 101 102])
      ;; Do not resolve a card by replacing card it. And handle idx-to-resolve running paste idx-to-replace.
      (concat
        [{:id 1, :types [:mov], :cost 4}
         {:id 2, :types [:mov], :cost 6}
         {:id 3, :provides-effect [:dmg], :cost 5}
         {:id 4, :provides-effect [:dmg], :cost 7}
         {:id 5, :provides-effect [:dmg], :cost 8}
         {:id 6, :provides-effect [:dmg], :cost 9}
         {:id 7, :cost 3}
         {:id 8, :cost 10}]
        (card-range 9 16)
        [{:id 16, :requires-type [:foo]}                    ;; Will be selected as the last card.
         {:id 17, :types [:foo], :requires-type [:bar]}     ;; Will not be resolved in first run.
         {:id 18, :types [:bar]}]
        (card-range 101 105))
      (concat (range 1 14) [18 17 16 101 102]))))
