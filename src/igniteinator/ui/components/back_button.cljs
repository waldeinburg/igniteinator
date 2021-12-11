(ns igniteinator.ui.components.back-button
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.text :refer [txt-c]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.icons.arrow-back :refer [arrow-back]]))

(defn back-button [{:as button-props}]
  (if (<sub :page-history-not-empty?)
    [tooltip (txt-c :back)
     [button (merge
               {:variant  :outlined
                :on-click #(>evt :page/pop)}
               button-props)
      [arrow-back]]]))
