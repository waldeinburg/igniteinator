(ns igniteinator.util.sort-test
  (:require
    [cljs.test :refer-macros [deftest is are testing]]
    [clojure.math.combinatorics :as combo]
    [igniteinator.util.sort :refer
     [keyfn-comparator sort-by-hierarchy]]))

(defn m [res items]
  (map-indexed
    (fn [idx item]
      (let [new-res (conj res item)
            other   (concat
                      (take idx items)
                      (take-last (- (count items) idx 1) items))]
        (if (empty? other)
          new-res
          (m new-res other))))
    items))

(let [a1b1       {:a 1 :b 1}
      a1b2       {:a 1 :b 2}
      a2b1       {:a 2 :b 1}
      a2b2       {:a 2 :b 2}
      all-perms  (combo/permutations [a1b1 a1b2 a2b1 a2b2])
      comp-a     (keyfn-comparator :a)
      comp-a-rev (keyfn-comparator :a true)
      comp-b     (keyfn-comparator :b)
      comp-b-rev (keyfn-comparator :b true)]

  (deftest keyfn-comparator-test
    (are [comp a b res]
      (= (comp a b) res)
      comp-a a1b2 a1b2 0
      comp-a a1b2 a2b1 -1
      comp-a a2b1 a1b2 1
      comp-b a1b2 a1b2 0
      comp-b a1b2 a2b1 1
      comp-b a2b1 a1b2 -1
      comp-a-rev a1b2 a1b2 0
      comp-a-rev a1b2 a2b1 1
      comp-a-rev a2b1 a1b2 -1
      comp-b-rev a1b2 a1b2 0
      comp-b-rev a1b2 a2b1 -1
      comp-b-rev a2b1 a1b2 1))

  (deftest sort-by-hierarchy-test-none
    (doseq [l all-perms]
      (is (= (sort-by-hierarchy [] l) l))))

  (deftest sort-by-hierarchy-test-one
    (are [comp values result]
      (= (sort-by-hierarchy [comp] values) result)
      comp-a [a1b1 a1b2 a2b1 a2b2] [a1b1 a1b2 a2b1 a2b2]
      comp-a [a1b2 a1b1 a2b2 a2b1] [a1b2 a1b1 a2b2 a2b1]
      comp-a [a2b1 a2b2 a1b1 a1b2] [a1b1 a1b2 a2b1 a2b2]
      comp-a [a2b2 a2b1 a1b2 a1b1] [a1b2 a1b1 a2b2 a2b1]))

  (deftest sort-by-hierarchy-test-multi
    (doseq [l all-perms]
      (is (= (sort-by-hierarchy [comp-a comp-b] l) [a1b1 a1b2 a2b1 a2b2]))
      (is (= (sort-by-hierarchy [comp-b comp-a] l) [a1b1 a2b1 a1b2 a2b2]))
      (is (= (sort-by-hierarchy [comp-a-rev comp-b] l) [a2b1 a2b2 a1b1 a1b2]))
      (is (= (sort-by-hierarchy [comp-a comp-b-rev] l) [a1b2 a1b1 a2b2 a2b1])))))
