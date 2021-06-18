(ns igniteinator.ui.main
  (:require [igniteinator.state :refer [state]]
            [reagent.core :as r]
            [reagent-material-ui.core.css-baseline :refer [css-baseline]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]))

(defn content []
  (let [mode (r/cursor state [:mode])]
    (fn []
      (condp = @mode
        :init [:div "The monkeys are listening ..."]
        :loading [circular-progress]
        :ready [:div "ready"]
        :fatal-error [:div (:fatal-message @state)]
        [:div (str "No such mode: " @mode)]))))

(defn main []
  [:<>
   [css-baseline]
   [content]])
