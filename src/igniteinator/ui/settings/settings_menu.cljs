(ns igniteinator.ui.settings.settings-menu
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.ui.settings.clear-data-button :refer [clear-data-button]]
            [goog.dom :as gdom]
            [reagent-material-ui.core.menu :refer [menu]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.settings :refer [settings] :rename {settings settings-icon}]))

(defn settings-menu [open?]
  (let [on-close #(>evt :set-settings-menu-open? false)]
    [menu {:id            :settings-menu
           :open          open?
           :on-close      on-close
           :anchor-el     #(gdom/getElement (name :settings-button))
           :MenuListProps {:aria-labelledby :settings-button}}
     [menu-item [clear-data-button]]]))

(defn settings-button []
  (let [open? (<sub :settings-menu-open?)]
    [:<>
     [tooltip (txt :settings-button-tooltip)
      [icon-button {:id            :settings-button
                    :aria-controls :settings-menu
                    :aria-haspopup true
                    :aria-expanded open?
                    :on-click      #(>evt :set-settings-menu-open? true)}
       [settings-icon]]]
     [settings-menu open?]]))
