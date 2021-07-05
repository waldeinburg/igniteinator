(ns igniteinator.model.card-filter
  (:require [clojure.string :as s]))

(defonce filters
  {
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
