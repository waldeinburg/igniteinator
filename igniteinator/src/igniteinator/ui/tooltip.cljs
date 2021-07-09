(ns igniteinator.ui.tooltip
  (:require [reagent.core :as r]
            [reagent-material-ui.core.tooltip :as mui]))

(defn tooltip [title child]
  "Standardized tooltip"
  [mui/tooltip {:title title, :arrow true} child])
