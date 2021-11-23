(ns igniteinator.ui.components.wait-modal
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [reagent-mui.material.backdrop :refer [backdrop]]
            [reagent-mui.material.circular-progress :refer [circular-progress]]))

;; TODO: with-styles is deprecated and the value should be from theme (https://v4.mui.com/components/backdrop/)
#_(def top-backdrop ((styles/with-styles {:root {:z-index 999}}) backdrop))
(def top-backdrop backdrop)

(defn waiting-modal []
  [top-backdrop {:open (<sub :waiting?)}
   [circular-progress {:size 100}]])
