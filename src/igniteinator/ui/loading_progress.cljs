(ns igniteinator.ui.loading-progress
  (:require [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]))

(defn loading-progress []
  [box {:display         :flex
        :height          200
        :justify-content :center
        :align-items     :center}
   [circular-progress {:size 100}]])
