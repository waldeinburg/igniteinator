(ns igniteinator.model.cards
  (:require [igniteinator.state :refer [data]]
            [igniteinator.util.filter :refer [filter-multi]]
            [igniteinator.util.sort :refer [reverse-comparator sort-by-hierarchy]]
            [igniteinator.model.card-filter :refer [filters]]
            [igniteinator.model.card-sort :refer [sortings]]
            [reagent.core :as r]))

(defonce cards (r/cursor data [:cards]))

(defn- filter-specs->preds [filter-specs]
  (map (fn [spec]
         (apply (-> :key spec filters) (:args spec)))
    filter-specs))

(defn- sorting-specs->comparators [sorting-specs]
  (map (fn [spec]
         (let [c (-> :key spec sortings)]
           (case (:order spec)
             :asc c
             :desc (reverse-comparator c))))
    sorting-specs))

(defn get-all-cards []
  (vals @cards))

(defn get-card-from-id [id]
  (get @cards id))

(defn get-cards-from-ids [ids]
  (vals (select-keys @cards ids)))

(defn get-cards [base-spec filter-specs sorting-specs]
  "Get cards based on filters and sortings from model.
  Base is :all or a sequence of ids.
  Arg filter-specs can be a single map {:key ..., args ...} or an sequence of those, with :key
  referring into card-filter/filters. If a single map, then :key must refer to a function that in
  itself returns the cards.
  Arg sortings-specs must be a sequence of {:key ..., :order ...}, with :key referring into
  card-sort/sortings and :order being :asc or :desc."
  (let [base  (case base-spec
                :all (get-all-cards)
                (get-cards-from-ids base-spec))
        preds (filter-specs->preds filter-specs)
        comps (sorting-specs->comparators sorting-specs)]
    (sort-by-hierarchy comps (filter-multi preds base))))
