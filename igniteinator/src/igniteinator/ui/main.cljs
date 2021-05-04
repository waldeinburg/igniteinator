(ns igniteinator.ui.main
  (:require [reagent-material-ui.core.css-baseline :refer [css-baseline]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]))

(defn main []
  [:<>
   [css-baseline]
   [circular-progress]])
