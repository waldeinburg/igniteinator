(ns igniteinator.util.filter-test
  (:require [clojure.test :refer [deftest is are testing]]
            [igniteinator.util.filter :refer [filter-multi]]))

(deftest filter-multi-test
  (let [coll [1 2 3 4]]
    (are [preds result]
      (= (filter-multi preds coll) result)
      [] coll
      [(constantly true)] coll
      [(constantly false)] []
      [(constantly true) (constantly true)] coll
      [(constantly false) (constantly false)] []
      [(constantly false) (constantly true)] []
      [(constantly true) (constantly false)] [])))
