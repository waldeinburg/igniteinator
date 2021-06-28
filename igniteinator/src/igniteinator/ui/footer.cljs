(ns igniteinator.ui.footer
  (:require [igniteinator.constants :refer [page-url]]
            [reagent-material-ui.core.link :refer [link]]))

(defn footer []
  [:footer
   [:p "All data and images are Copyright © 2021, "
    [link {:href   "http://gingersnapgaming.com"
           :target "_blank"
           :rel    "noreferrer"}
     "Ginger Snap Gaming"]
    ". Used in the " [link {:href page-url} "Igniteinator"] " by permission."]])
