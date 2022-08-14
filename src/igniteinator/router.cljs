(ns igniteinator.router
  (:require [bide.core :as r]
            [igniteinator.util.re-frame :refer [>evt]]))

(def router
  (r/router [["/" :front]
             ["/cards/" :cards]
             ["/setups/" :setups]
             ["/epic/" :epic]]))

(defn on-navigate [name params query]
  (>evt :route name params query))

(defn start! []
  (r/start! router {:default     :front
                    :on-navigate on-navigate
                    :html5?      true}))

(defn navigate! [id params query]
  (r/navigate! router id params query))

(defn replace! [id params query]
  (r/replace! router id params query))
