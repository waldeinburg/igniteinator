(ns igniteinator.util.sort)

(defn keyfn-comparator [keyfn]
  "Return a comparator that compares (keyfn item) with compare."
  (fn [x y]
    (compare (keyfn x) (keyfn y))))

(defn reverse-comparator [comp]
  (fn [x y]
    (* -1 (comp x y))))

(defn comp-hierarchy-comparator [comps]
  "Returns a comparator from a vector of comparators.
   The resulting comparator will iterate through the comparators until a non-zero result is found
   and return that value or return zero if no comparison returns a non-zero value."
  (fn [x y]
    (let [res (some #(let [r (% x y)]
                       (and (not (zero? r)) r))
                comps)]
      (or res 0))))

(defn sort-by-hierarchy [comps coll]
  "Sort by a comparator returned by (comp-hierarchy-comparator comparators)."
  (sort (comp-hierarchy-comparator comps) coll))
