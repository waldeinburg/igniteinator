(ns igniteinator.router
  (:require [bide.core :as r]
            [igniteinator.util.re-frame :refer [>evt]]))

(def router
  (r/router [["/" :cards]
             ["/cards" :cards]
             ["/setups" :setups]
             ["/epic" :epic]]))

(defn on-navigate [name params query]
  (>evt :route name params query))

(defn start []
  (r/start! router {:default     :cards
                    :on-navigate on-navigate
                    :html5?      true}))
