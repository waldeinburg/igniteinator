(ns igniteinator.ui.components.loading-progress
  (:require [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.circular-progress :refer [circular-progress]]))

(defn loading-progress []
  [box {:display         :flex
        :height          200
        :justify-content :center
        :align-items     :center}
   [circular-progress {:size 100}]])
