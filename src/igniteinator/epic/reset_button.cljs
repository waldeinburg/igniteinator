(ns igniteinator.epic.reset-button
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [>evt]]
            [igniteinator.ui.components.button-with-confirm-dialog :refer [button-with-confirm-dialog]]))

(defn reset-button []
  [button-with-confirm-dialog
   {:button-text           (txt :epic/reset-button-text)
    :dialog-title          (txt :epic/reset-dialog-title)
    :dialog-text           (txt :epic/reset-dialog-text)
    :dialog-open-sub       :epic/reset-dialog-open?
    :set-dialog-open-event :epic/set-reset-dialog-open?
    :on-confirm            #(>evt :epic/reset)
    :button-color          :secondary
    :button-sx             {:mr 2}}])
