(ns igniteinator.util.filter)

(defn filter-multi [preds coll]
  "Filter with multiple predicates."
  (if (empty? preds)
    coll
    (filter (fn [x]
              (every? #(% x) preds))
      coll)))
