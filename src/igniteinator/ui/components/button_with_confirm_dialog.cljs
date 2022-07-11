(ns igniteinator.ui.components.button-with-confirm-dialog
  (:require [igniteinator.util.re-frame :refer [<sub-ref >evt]]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.dialog :refer [dialog]]
            [reagent-mui.material.button :refer [button]]))

(defn confirm-dialog [{:keys [dialog-title dialog-text
                              dialog-open-sub set-dialog-open-event on-dialog-close
                              on-confirm]}]
  (let [on-close (fn []
                   (if on-dialog-close
                     on-dialog-close)
                   (>evt set-dialog-open-event false))]
    [dialog {:title     dialog-title
             :open?-ref (<sub-ref dialog-open-sub)
             :on-close  on-close
             :buttons   [[button {:on-click (fn [_]
                                              (on-confirm)
                                              (on-close))
                                  :variant  :contained
                                  :color    :primary}
                          (txt-c :ok)]
                         [button {:on-click on-close
                                  :variant  :contained
                                  :color    :secondary}
                          (txt-c :cancel)]]}
     [:p dialog-text]]))

(defn button-with-confirm-dialog [{:keys [button-text set-dialog-open-event
                                          button-color button-sx]
                                   :or   {button-color :primary}
                                   :as   props}]
  [:<>
   [confirm-dialog (dissoc props :button-text)]
   [button {:variant  :outlined
            :on-click #(>evt set-dialog-open-event true)
            :color    button-color
            :sx       button-sx}
    button-text]])
