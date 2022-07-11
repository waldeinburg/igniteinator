(ns igniteinator.ui.settings.clear-data-button
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.button-with-confirm-dialog :refer [button-with-confirm-dialog]]
            [igniteinator.util.re-frame :refer [>evt]]))

(defn clear-data-button []
  [button-with-confirm-dialog
   {:button-text           (txt :clear-data/button-text)
    :dialog-title          (txt :clear-data/dialog-title)
    :dialog-text           (txt :clear-data/dialog-text)
    :dialog-open-sub       :clear-data/dialog-open?
    :set-dialog-open-event :clear-data/set-dialog-open?
    :on-dialog-close       #(>evt :set-settings-menu-open? false)
    :on-confirm            #(>evt :clear-data)}])
