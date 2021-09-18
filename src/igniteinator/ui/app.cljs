(ns igniteinator.ui.app
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [igniteinator.ui.cards-page :refer [cards-page]]
            [igniteinator.ui.header :refer [header]]
            [igniteinator.ui.footer :refer [footer]]
            [igniteinator.ui.caching-progress :refer [caching-progress]]
            [igniteinator.ui.card-details :refer [card-details-page]]
            [reagent-material-ui.core.css-baseline :refer [css-baseline]]
            [reagent-material-ui.core.container :refer [container]]
            [reagent-material-ui.core.circular-progress :refer [circular-progress]]))

(defn pages []
  ;; TODO: add router here?
  (let [page (<sub :current-page)]
    (case page
      :cards [cards-page]
      :card-details [card-details-page])))

(defn content []
  (let [mode (<sub :mode)]
    (case mode
      :init [:div "The monkeys are listening ..."]
      :loading [circular-progress]
      :ready [pages]
      :fatal-error [:div (<sub :fatal-message)]
      [:div (str "No such mode: " mode)])))

(defn app []
  [:<>
   [css-baseline]
   [caching-progress]
   [container
    [header]
    [content]
    [footer]]])
