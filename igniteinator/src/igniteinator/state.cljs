(ns igniteinator.state
  (:require [reagent.core :as r]))

(defonce state (r/atom {
                        :data     nil
                        :language :en
                        :mode     :init
                        }))

;; Easy access to language cursor.
(defonce language (r/cursor state [:language]))

(defn set-state! [& kvs]
  (swap! state #(apply assoc % kvs)))
