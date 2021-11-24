(ns igniteinator.ui.components.wait-modal
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [reagent-mui.material.backdrop :refer [backdrop]]
            [reagent-mui.material.circular-progress :refer [circular-progress]]))

(defn waiting-modal []
  [backdrop {:open (<sub :waiting?)}
   [circular-progress {:size 100}]])
