(ns igniteinator.ui.header
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.link :refer [external-link]]
            [igniteinator.constants :as const]))

(defn header []
  [:header
   [:h1 "Igniteinator"]
   [:div.subtitle "– " (txt :subtitle) " " [external-link const/ignite-link "Ignite"]]])
