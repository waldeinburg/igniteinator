(ns igniteinator.ui.main
  (:require [igniteinator.state :refer [data state]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.ui.header :refer [header]]
            [igniteinator.ui.footer :refer [footer]]
            [reagent.core :as r]
            [reagent-material-ui.core.css-baseline :refer [css-baseline]]
            [reagent-material-ui.core.container :refer [container]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]))

(defn content []
  (let [mode (r/cursor state [:mode])]
    (fn []
      (println "foo")
      (condp = @mode
        :init [:div "The monkeys are listening ..."]
        :loading [circular-progress]
        :ready [card-list (vals (:cards @data))]
        :fatal-error [:div (:fatal-message @state)]
        [:div (str "No such mode: " @mode)]))))

(defn main []
  [:<>
   [css-baseline]
   [container
    [header]
    [content]
    [footer]]])
