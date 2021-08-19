(ns igniteinator.model.card-filter
  (:require [clojure.string :as s]
            [igniteinator.util.string :as ss]))



(def filters
  {
   ;; Regular expressions; not in the way for those who just type in characters, cool for those who
   ;; discover the feature and understand it.
   :name-contains (fn [re-str]
                    (if (empty? re-str)
                      ;; No value mean no filtering.
                      (constantly true)
                      (if-let [re (-> re-str s/lower-case ss/re-pattern-no-error)]
                        #(re-find re (s/lower-case (:name %)))
                        ;; Invalid regexp? Match literal.
                        #(s/index-of (:name %) re-str))))
   :has-combos    (fn [] #(not-empty (:combos %)))
   :cost          (fn [operator value]
                    (let [o (case operator := =, :< <, :> >, :<= <=, :>= >=)]
                      ;; title cards will always be filtered away.
                      #(o (:cost %) value)))
   })
