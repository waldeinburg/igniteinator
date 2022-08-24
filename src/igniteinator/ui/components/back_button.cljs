(ns igniteinator.ui.components.back-button
  (:require [igniteinator.text :refer [txt-c]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.icons.arrow-back :refer [arrow-back]]
            [reagent-mui.material.button :refer [button]]))

(defn back-button [{:as button-props}]
  (if (<sub :back-page)
    [tooltip (txt-c :back)
     [button (merge
               {:variant  :outlined
                :on-click #(>evt :page/back)}
               button-props)
      [arrow-back]]]))
