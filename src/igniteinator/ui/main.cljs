(ns igniteinator.ui.main
  (:require [igniteinator.state :refer [state]]
            [igniteinator.ui.cards-page :refer [cards-page]]
            [igniteinator.ui.header :refer [header]]
            [igniteinator.ui.footer :refer [footer]]
            [igniteinator.ui.caching-progress :refer [caching-progress]]
            [igniteinator.ui.combos :refer [combos-page]]
            [reagent.core :as r]
            [reagent-material-ui.core.css-baseline :refer [css-baseline]]
            [reagent-material-ui.core.container :refer [container]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]))

(defn pages []
  ;; TODO: add router here?
  [:<>
   [cards-page]
   [combos-page]])

(defn content []
  (let [mode (r/cursor state [:mode])]
    (fn []
      (case @mode
        :init [:div "The monkeys are listening ..."]
        :loading [circular-progress]
        :ready [pages]
        :fatal-error [:div (:fatal-message @state)]
        [:div (str "No such mode: " @mode)]))))

(defn main []
  [:<>
   [css-baseline]
   [caching-progress]
   [container
    [header]
    [content]
    [footer]]])
