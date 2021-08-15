(ns igniteinator.state
  (:require [reagent.core :as r]))

;; State object. Nil values are just for documentation.
(defonce state
  (r/atom
    {
     :language       :en
     :mode           :init
     :current-page   :cards
     :previous-page  nil
     :card-size      :normal
     :install-dialog {:open? false}
     :cards-page     {:base           :all
                      :filters        []
                      :sortings       [{:key :name, :order :asc}]
                      :search-str     ""
                      :card-selection {:dialog-open? false
                                       :ids          #{}
                                       :search-str   ""}}
     :combos-page    {:card-id  nil
                      :sortings [{:key :name, :order :asc}]}
     }))

;; Data object is stored in a separate atom to limit the amount of data to serialize each time state
;; is updated. The data object is static as soon as the app is fully loaded.
(defonce data (r/atom {}))

;; Easy access to language cursor.
(defonce language (r/cursor state [:language]))

(defn assoc-a! [a & kvs]
  (swap! a #(apply assoc % kvs)))

(defn assoc-in-a! [a & path-vs]
  (swap! a (fn [initial]
             (reduce (fn [st [p v]]
                       (assoc-in st p v))
               initial
               (partition 2 path-vs)))))

(defn set-state! [& kvs]
  (apply assoc-a! state kvs))

(defn set-in-state! [& path-vs]
  (apply assoc-in-a! state path-vs))
