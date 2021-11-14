(ns igniteinator.ui.settings.settings-menu
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.settings.clear-data-button :refer [clear-data-button]]
            [igniteinator.ui.settings.size :refer [size-settings]]
            [reagent-material-ui.core.drawer :refer [drawer]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.core.list :refer [list]]
            [reagent-material-ui.core.list-item :refer [list-item]]
            [reagent-material-ui.core.divider :refer [divider]]
            [reagent-material-ui.icons.settings :refer [settings] :rename {settings settings-icon}]))

(defn settings-button []
  [:<>
   [icon-button {:on-click #(>evt :set-settings-menu-open? true)}
    [settings-icon]]
   [drawer {:anchor   :right
            :open     (<sub :settings-menu-open?)
            :on-close #(>evt :set-settings-menu-open? false)}
    [list
     [list-item [size-settings]]
     [divider]
     [list-item [clear-data-button]]]]])
