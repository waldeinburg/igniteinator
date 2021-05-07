(ns igniteinator.util.sort)

(defn keyfn-comparator
  "Return a comparator that compares (keyfn item) with compare.
   If reverse? is true the items will be compared in reverse order."
  ([keyfn]
   (keyfn-comparator keyfn false))
  ([keyfn reverse?]
   (if reverse?
     #(compare (keyfn %2) (keyfn %1))
     #(compare (keyfn %1) (keyfn %2)))))

(defn comp-hierarchy-comparator [comps]
  "Returns a comparator from a vector of comparators.
   The resulting comparator will iterate through the comparators until a non-zero result is found
   and return that value or return zero if no comparison returns a non-zero value."
  (fn [x y]
    ;; Reduce comparators, terminating if the previous returned a non-zero result.
    @(ensure-reduced
       (reduce
         (fn [previous-result comp]
           (if (not= 0 previous-result)
             (reduced previous-result)
             (comp x y)))
         0
         comps))))

(defn sort-by-hierarchy [comps coll]
  "Sort by a comparator returned by (comp-hierarchy-comparator comparators)."
  (sort (comp-hierarchy-comparator comps) coll))
