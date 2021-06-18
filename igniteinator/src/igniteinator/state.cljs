(ns igniteinator.state
  (:require [reagent.core :as r]))

(defonce state (r/atom {
                        :data     nil
                        :language :en
                        :mode     :init
                        }))

(defn set-state! [& kvs]
  (swap! state #(apply assoc % kvs)))
