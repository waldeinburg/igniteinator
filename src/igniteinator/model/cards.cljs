(ns igniteinator.model.cards
  (:require [igniteinator.util.filter :refer [filter-multi]]
            [igniteinator.util.sort :refer [reverse-comparator sort-by-hierarchy]]
            [igniteinator.model.card-filter :refer [filters]]
            [igniteinator.model.card-sort :refer [sortings]]
            [reagent.core :as r]))

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

