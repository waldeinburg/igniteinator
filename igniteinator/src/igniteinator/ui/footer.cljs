(ns igniteinator.ui.footer
  (:require [igniteinator.constants :refer [page-url]]))

(defn footer []
  [:footer "All data and images are Copyright © 2021, "
   [:a {:href "http://gingersnapgaming.com"} "Ginger Snap Gaming"]
   ". Used in the " [:a {:href page-url} "Igniteinator"] " by permission."])
