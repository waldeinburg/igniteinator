(ns igniteinator.util.filter)

(defn filter-multi [preds coll]
  "Filter with multiple predicates."
  (if (empty? preds)
    coll
    (filter (apply every-pred preds)
      coll)))

(defn cost= [cost card]
  (= cost (:cost card)))

(defn cost>=< [cost-from cost-to card]
  (<= cost-from (:cost card) cost-to))

(defn cost<= [cost card]
  (>= cost (:cost card)))

(defn cost>= [cost card]
  (<= cost (:cost card)))

(defn find-id-by-name-fn [coll]
  (fn [name]
    (some #(if (= name (:name %))
             (:id %))
      coll)))
