(ns igniteinator.ui.pages.front-page
  (:require [igniteinator.constants :as const]
            [igniteinator.router :refer [resolve-to-href]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.link :refer [external-link]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.event :refer [prevent-default]]
            [igniteinator.util.re-frame :refer [>evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [reagent-mui.icons.settings :refer [settings] :rename {settings settings-icon}]
            [reagent-mui.material.list-item-text :refer [list-item-text]]
            [reagent-mui.material.menu-item :refer [menu-item]]
            [reagent-mui.material.menu-list :refer [menu-list]]
            [reagent-mui.material.paper :refer [paper]]))

(def menu-items [[:cards :cards-page-title
                  "Browse cards and combos"]
                 [:setups :setups-page-title
                  "See the designer's suggested setups"]
                 [:randomizer :randomizer/page-title
                  "Generate a good market automatically"]
                 [:epic :epic/page-title
                  "Play the Epic Ignite variants without sorting all of your cards"]])

(defn page-menu []
  [paper {:elevation 6}
   [menu-list
    (add-children
      (map
        (fn [[page-key title-key description]]
          [menu-item
           {:component :a
            :href      (resolve-to-href page-key)
            :sx        {:white-space :normal}
            :on-click  (fn [event]
                         (prevent-default event)
                         (>evt :page/navigate page-key))}
           [list-item-text {:sx {:flex "0 0 17ch"}}         ; "Suggested setups" is 16 characters plus some space
            [:strong (txt title-key)]]
           [list-item-text {:sx {:flex "0 1 auto"}}
            description]])
        menu-items))]])

(defn front-page []
  (page "Welcome to the Igniteinator" {:site-subtitle nil}
    [:p "This is a fan-made app for the skirmish deck building board game "
     [external-link const/ignite-link "Ignite"] "."]
    [:p "Use the "
     [settings-icon {:font-size :inherit
                     :sx        {:cursor :pointer}
                     :on-click  #(>evt :set-settings-menu-open? true)}]
     " button to configure which boxes you own."]
    [page-menu]))
