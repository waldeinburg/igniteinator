(ns igniteinator.model.cards
  (:require [igniteinator.util.filter :refer [filter-multi]]
            [igniteinator.util.sort :refer [reverse-comparator sort-by-hierarchy]]
            [igniteinator.model.card-filter :refer [filters get-all-cards]]
            [igniteinator.model.card-sort :refer [sortings]]))

(defn filter-specs->preds [filter-specs]
  (map (fn [spec]
         (apply (-> :key spec filters) (:args spec)))
    filter-specs))

(defn sorting-specs->comparators [sorting-specs]
  (map (fn [spec]
         (let [c (-> :key spec sortings)]
           (case (:order spec)
             :asc c
             :desc (reverse-comparator c))))
    sorting-specs))

(defn get-cards [filter-specs sorting-specs]
  "Get cards based on filters and sortings from model.
  Arg filter-specs can be a single map {:key ..., args ...} or an sequence of those, with :key
  referring into card-filter/filters. If a single map, then :key must refer to a function that in
  itself returns the cards.
  Arg sortings-specs must be a sequence of {:key ..., :order ...}, with :key referring into
  card-sort/sortings and :order being :asc or :desc."
  (sort-by-hierarchy (sorting-specs->comparators sorting-specs)
    (if (map? filter-specs)
      (apply (-> :key filter-specs filters) (:args filter-specs))
      (filter-multi (filter-specs->preds filter-specs) (get-all-cards)))))
