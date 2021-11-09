(ns igniteinator.ui.app
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [igniteinator.ui.pages.cards-page :refer [cards-page]]
            [igniteinator.ui.header :refer [header]]
            [igniteinator.ui.footer :refer [footer]]
            [igniteinator.ui.components.loading-progress :refer [loading-progress]]
            [igniteinator.ui.components.wait-modal :refer [waiting-modal]]
            [igniteinator.ui.singletons.reload-snackbar :refer [reload-snackbar]]
            [igniteinator.ui.singletons.caching-progress :refer [caching-progress]]
            [igniteinator.ui.pages.card-details :refer [card-details-page]]
            [igniteinator.ui.pages.setups-page :refer [setups-page]]
            [igniteinator.ui.pages.display-setup-page :refer [display-setup-page]]
            [reagent-material-ui.core.css-baseline :refer [css-baseline]]
            [reagent-material-ui.core.container :refer [container]]))

(defn pages []
  ;; TODO: add router here?
  (let [page (<sub :current-page)]
    (case page
      :cards [cards-page]
      :card-details [card-details-page]
      :setups [setups-page]
      :display-setup [display-setup-page])))

(defn content []
  (let [mode (<sub :mode)]
    (case mode
      :init [:div "The monkeys are listening ..."]
      :loading [loading-progress]
      :ready [pages]
      :fatal-error [:div (<sub :fatal-message)]
      [:div (str "No such mode: " mode)])))

(defn app []
  [:<>
   [css-baseline]
   [container
    [header]
    [content]
    [footer]]
   [caching-progress]
   [reload-snackbar]
   [waiting-modal]])
