(ns igniteinator.ui.header
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.link :refer [external-link]]
            [igniteinator.constants :as const]
            [igniteinator.ui.install-button :refer [install-button]]
            [reagent-material-ui.core.box :refer [box]]))

(defn header []
  [box {:display :flex}
   [:header
    [:h1 "Igniteinator"]
    [:div.subtitle "– " (txt :subtitle) " " [external-link const/ignite-link "Ignite"]]]
   [box {:ml "auto"}
    [install-button]]])
