(ns igniteinator.ui.settings.settings-menu
  (:require [igniteinator.ui.components.menu-button :refer [menu-button]]
            [igniteinator.ui.settings.clear-data-button :refer [clear-data-button]]
            [igniteinator.ui.settings.size :refer [size-settings]]
            [reagent-material-ui.core.divider :refer [divider]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.icons.settings :refer [settings] :rename {settings settings-icon}]))

(defn settings-button []
  [menu-button {:button-id     :settings-button
                :menu-id       :settings-menu
                :open?-sub     :settings-menu-open?
                :set-open?-evt :set-settings-menu-open?
                :tooltip-key   :settings-button-tooltip
                :icon          settings-icon}
   [size-settings]
   [divider]
   [menu-item [clear-data-button]]])
