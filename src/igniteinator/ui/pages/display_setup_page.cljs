(ns igniteinator.ui.pages.display-setup-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.icons.file-copy :refer [file-copy]]))

(defn copy-to-cards-page-button []
  [tooltip (txt :copy-to-cards-page-tooltip)
   [button {:variant  :outlined
            :on-click #(>evt :current-setup/copy-to-cards-page)}
    [box {:component "span", :mr 0.5} [file-copy]]
    (txt :copy-to-cards-page-button)]])

(defn display-setup-page []
  (let [name      (<sub :current-setup/name)
        cards     (<sub :current-setup/cards)
        boxes-str (<sub :current-setup/required-boxes-string)]
    [page name
     [toolbar [back-button] [copy-to-cards-page-button]]
     [:p (str (txt :required-boxes) ": " boxes-str)]
     [card-list cards]]))
