(ns igniteinator.ui.settings.settings-menu
  (:require [igniteinator.ui.settings.boxes :refer [boxes-settings]]
            [igniteinator.ui.settings.clear-data-button :refer [clear-data-button]]
            [igniteinator.ui.settings.default-order :refer [default-order-settings]]
            [igniteinator.ui.settings.display-name :refer [display-name-settings]]
            [igniteinator.ui.settings.epic-trash-mode :refer [epic-trash-mode-setting]]
            [igniteinator.ui.settings.size :refer [size-settings]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.icons.settings :refer [settings] :rename {settings settings-icon}]
            [reagent-mui.material.divider :refer [divider]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.material.list :refer [list]]
            [reagent-mui.material.list-item :refer [list-item]]
            [reagent-mui.material.swipeable-drawer :refer [swipeable-drawer]]))

(defn setting [elem]
  [:<>
   [list-item elem]
   [divider]])

(defn settings-button []
  [:<>
   [icon-button {:on-click #(>evt :set-settings-menu-open? true)}
    [settings-icon]]
   [swipeable-drawer {:anchor   :right
                      :open     (<sub :settings-menu-open?)
                      :on-close #(>evt :set-settings-menu-open? false)
                      :on-open  #(>evt :set-settings-menu-open? true)}
    [list
     [setting [boxes-settings]]
     [setting [size-settings]]
     [setting [default-order-settings]]
     [setting [display-name-settings]]
     [setting [epic-trash-mode-setting]]
     [list-item [clear-data-button]]]]])
