(ns igniteinator.state
  (:require [reagent.core :as r]))

(def state (r/atom {
                    :data     nil
                    :language :en
                    :mode     :init
                    }))

(defn set-state! [& kvs]
  (swap! state #(apply assoc % kvs)))
