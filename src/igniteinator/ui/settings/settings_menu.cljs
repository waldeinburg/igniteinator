(ns igniteinator.ui.settings.settings-menu
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.settings.clear-data-button :refer [clear-data-button]]
            [igniteinator.ui.settings.boxes :refer [boxes-settings]]
            [igniteinator.ui.settings.size :refer [size-settings]]
            [igniteinator.ui.settings.default-order :refer [default-order-settings]]
            [igniteinator.ui.settings.display-name :refer [display-name-settings]]
            [reagent-material-ui.core.drawer :refer [drawer]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.core.list :refer [list]]
            [reagent-material-ui.core.list-item :refer [list-item]]
            [reagent-material-ui.core.divider :refer [divider]]
            [reagent-material-ui.icons.settings :refer [settings] :rename {settings settings-icon}]))

(defn setting [elem]
  [:<>
   [list-item elem]
   [divider]])

(defn settings-button []
  [:<>
   [icon-button {:on-click #(>evt :set-settings-menu-open? true)}
    [settings-icon]]
   [drawer {:anchor   :right
            :open     (<sub :settings-menu-open?)
            :on-close #(>evt :set-settings-menu-open? false)}
    [list
     [setting [boxes-settings]]
     [setting [size-settings]]
     [setting [default-order-settings]]
     [setting [display-name-settings]]
     [list-item [clear-data-button]]]]])
