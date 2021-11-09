(ns igniteinator.ui.footer
  (:require [igniteinator.constants :refer [version page-url github-url]]
            [igniteinator.ui.components.link :refer [external-link]]
            [reagent-material-ui.core.link :refer [link]]
            [reagent-material-ui.core.box :refer [box]]))

(defn footer []
  [box {:mt 6, :color :text.secondary}
   [:footer
    ;; No translation of this text.
    [:p
     "All data and images are Copyright © 2021, "
     [external-link "https://gingersnapgaming.com" "Ginger Snap Gaming"]
     ". Used in the " [link {:href page-url} "Igniteinator"] " by permission."]
    [:p "Source code available on " [external-link github-url "GitHub"] "."]
    [:p "Version " version "."]]])
