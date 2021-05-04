(ns igniteinator.util.sort)

(defn keyfn-hierarchy-comparator [keyfns]
  "Returns a comparator from a vector of keyfn functions.
   The comparator will compare (keyfn item) using compare until a non-zero result is found and
   return that value or return zero if no comparison returns a non-zero value."
  (fn [a b]
    ;; Reduce keyfn functions, terminating if one returns a non-zero result.
    @(ensure-reduced
       (reduce
         (fn [previous-result keyfn]
           (if (not= 0 previous-result)
             (reduced previous-result)
             (compare (keyfn a) (keyfn b))))
         0
         keyfns))))

(defn sort-by-hierarchy [keyfns coll]
  "Sort by a comparator returned by (keyfn-hierarchy-comparator keyfns)."
  (sort (keyfn-hierarchy-comparator keyfns) coll))
