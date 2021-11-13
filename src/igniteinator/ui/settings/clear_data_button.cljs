(ns igniteinator.ui.settings.clear-data-button
  (:require [igniteinator.util.re-frame :refer [<sub-ref >evt]]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.dialog :refer [dialog]]
            [reagent-material-ui.core.button :refer [button]]))

(defn clear-data-dialog []
  (let [on-close (fn []
                   (>evt :set-settings-menu-open? false)
                   (>evt :clear-data/set-dialog-open? false))]
    [dialog {:title     (txt :clear-data/dialog-title)
             :open?-ref (<sub-ref :clear-data/dialog-open?)
             :on-close  on-close
             :buttons   [[button {:on-click (fn [_]
                                              (>evt :clear-data)
                                              (on-close))
                                  :variant  :contained
                                  :color    :primary}
                          (txt-c :ok)]
                         [button {:on-click on-close
                                  :variant  :contained
                                  :color    :secondary}
                          (txt-c :cancel)]]}
     [:p (txt :clear-data/dialog-text)]]))

(defn clear-data-button []
  [:<>
   [clear-data-dialog]
   [button {:on-click #(>evt :clear-data/set-dialog-open? true)}
    (txt :clear-data/button-text)]])
