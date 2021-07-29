(ns igniteinator.ui.footer
  (:require [igniteinator.constants :refer [version page-url]]
            [igniteinator.ui.link :refer [external-link]]
            [reagent-material-ui.core.link :refer [link]]))

(defn footer []
  [:footer
   ;; No translation of this text.
   [:p
    "All data and images are Copyright © 2021, "
    [external-link "http://gingersnapgaming.com" "Ginger Snap Gaming"]
    ". Used in the " [link {:href page-url} "Igniteinator"] " by permission."
    " Version " version "."]])
