(ns igniteinator.ui.wait-modal
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [reagent-material-ui.styles :as styles]
            [reagent-material-ui.core.backdrop :refer [backdrop]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]))

;; TODO: with-styles is deprecated and the value should be from theme (https://v4.mui.com/components/backdrop/)
(def top-backdrop ((styles/with-styles {:root {:z-index 999}}) backdrop))

(defn waiting-modal []
  [top-backdrop {:open (<sub :waiting?)}
   [circular-progress {:size 100}]])
