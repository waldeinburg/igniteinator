(ns igniteinator.state
  (:require [reagent.core :as r]))

(defonce state
  (r/atom
    {
     :data      nil
     :language  :en
     :mode      :init
     :card-size :normal
     }))

;; Data object is stored in a separate atom to limit the amount of data to serialize each time state
;; is updated. The data object is static as soon as the app is fully loaded.
(defonce data (r/atom {}))

;; Easy access to language cursor.
(defonce language (r/cursor state [:language]))

(defn set-in! [a & kvs]
  (swap! a #(apply assoc % kvs)))

(defn set-state! [& kvs]
  (apply set-in! state kvs))
