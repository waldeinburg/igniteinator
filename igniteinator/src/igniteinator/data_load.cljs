(ns igniteinator.data-load
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [igniteinator.state :refer [set-state!]]
            [igniteinator.constants :refer [data-file-path]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.alert :as alert]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn data->cards [data]
  (reduce
    (fn [c v]
      (assoc c (:id v) v))
    {}
    (:cards data)))

(defn- set-data! [data]
  (let [cards (data->cards data)]
    (set-state!
      :data data
      :cards cards
      :mode :ready)))

(defn load-data []
  (go
    (set-state! :mode :loading)
    (let [response (<! (http/get data-file-path))]
      (if (:success response)
        (set-data! (:body response))
        (alert/fatal! (str (txt :data-load-error) ": " (:error-text response)))))))
