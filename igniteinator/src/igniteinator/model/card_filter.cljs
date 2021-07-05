(ns igniteinator.model.card-filter
  (:require [igniteinator.state :refer [data]]
            [reagent.core :as r]
            [clojure.string :as s]))

(defonce cards (r/cursor data [:cards]))

(defn cards-from-ids [ids]
  (vals (select-keys @cards ids)))

(defn get-all-cards []
  (vals @cards))

(defonce filters
  {
   ;; All and ids are inefficient to make as normal predicates.
   ;; The lesser clunky way to achieve it seems to be making cards/get-cards not know about the
   ;; possibilities but distinguish between being handed a key + arguments or a sequence of those.
   ;; The UI should name the filters.
   :all           #(get-all-cards)
   :ids           #(cards-from-ids %)
   ;; Regular expressions; not in the way for those who just type in characters, cool for those who
   ;; discover the feature and understand it.
   :name-contains (fn [re-str]
                    (let [re (-> re-str s/lower-case re-pattern)]
                      #(re-find re (s/lower-case (:name %)))))
   :has-combos    (fn [] #(not-empty (:combos %)))
   :cost          (fn [operator value]
                    (let [o (case operator := =, :< <, :> >, :<= <=, :>= >=)]
                      ;; title cards will always be filtered away.
                      #(o (:cost %) value)))
   })
