(ns igniteinator.ui.app
  (:require [igniteinator.ui.components.loading-progress :refer [loading-progress]]
            [igniteinator.ui.components.wait-modal :refer [waiting-modal]]
            [igniteinator.ui.footer :refer [footer]]
            [igniteinator.ui.header :refer [header]]
            [igniteinator.ui.hooks :refer [desktop-menu?-hook]]
            [igniteinator.ui.pages.card-details-page :refer [card-details-page]]
            [igniteinator.ui.pages.cards-page :refer [cards-page]]
            [igniteinator.ui.pages.display-setup-page :refer [display-setup-page]]
            [igniteinator.ui.pages.epic-pages :refer [display-stack-page epic-page trash-page]]
            [igniteinator.ui.pages.error-page :refer [error-page]]
            [igniteinator.ui.pages.front-page :refer [front-page]]
            [igniteinator.ui.pages.randomizer-data-page :refer [randomizer-data-page]]
            [igniteinator.ui.pages.randomizer-page :refer [randomizer-page]]
            [igniteinator.ui.pages.setups-page :refer [setups-page]]
            [igniteinator.ui.singletons.caching-progress :refer [caching-progress]]
            [igniteinator.ui.singletons.reload-snackbar :refer [reload-snackbar]]
            [igniteinator.util.re-frame :refer [<sub]]
            [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.css-baseline :refer [css-baseline]]))

(defn pages []
  (let [page (<sub :current-page)]
    (case page
      :front [front-page]
      :error [error-page]
      :cards [cards-page]
      :card-details [card-details-page]
      :setups [setups-page]
      :display-setup [display-setup-page]
      :epic [epic-page]
      :epic/display-stack [display-stack-page]
      :epic/trash [trash-page]
      :randomizer [randomizer-page]
      :randomizer/data [randomizer-data-page])))

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
