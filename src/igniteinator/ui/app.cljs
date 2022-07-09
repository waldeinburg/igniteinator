(ns igniteinator.ui.app
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [igniteinator.ui.pages.cards-page :refer [cards-page]]
            [igniteinator.ui.header :refer [header]]
            [igniteinator.ui.footer :refer [footer]]
            [igniteinator.ui.hooks :refer [desktop-menu?-hook]]
            [igniteinator.ui.components.loading-progress :refer [loading-progress]]
            [igniteinator.ui.components.wait-modal :refer [waiting-modal]]
            [igniteinator.ui.singletons.reload-snackbar :refer [reload-snackbar]]
            [igniteinator.ui.singletons.caching-progress :refer [caching-progress]]
            [igniteinator.ui.pages.card-details :refer [card-details-page]]
            [igniteinator.ui.pages.setups-page :refer [setups-page]]
            [igniteinator.ui.pages.display-setup-page :refer [display-setup-page]]
            [igniteinator.ui.pages.epic-page :refer [epic-page]]
            [reagent-mui.material.css-baseline :refer [css-baseline]]
            [reagent-mui.material.container :refer [container]]))

(defn pages []
  ;; TODO: add router here?
  (let [page (<sub :current-page)]
    (case page
      :cards [cards-page]
      :card-details [card-details-page]
      :setups [setups-page]
      :display-setup [display-setup-page]
      :epic [epic-page])))

(defn content []
  (let [mode (<sub :mode)]
    (case mode
      :init [:div "The monkeys are listening ..."]
      :loading [loading-progress]
      :ready [pages]
      :fatal-error [:div (<sub :fatal-message)]
      [:div (str "No such mode: " mode)])))

(defn main []
  [:f>
   (fn []
     (let [desktop-menu? (desktop-menu?-hook)]
       [:<>
        (if (not desktop-menu?)
          [header])
        [container
         (if desktop-menu?
           [header])
         [content]
         [footer]]]))])

(defn app []
  [:<>
   [css-baseline]
   [main]
   [caching-progress]
   [reload-snackbar]
   [waiting-modal]])
