(ns igniteinator.ui.components.tooltip
  (:require [reagent.core :as r]
            [reagent-mui.material.tooltip :as mui]))

(defn tooltip [title child]
  "Standardized tooltip"
  [mui/tooltip {:title title, :arrow true} child])
