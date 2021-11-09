(ns igniteinator.ui.components.back-button
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.text :refer [txt-c]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.icons.arrow-back :refer [arrow-back]]))

(defn back-button []
  (if (<sub :page-history-not-empty?)
    [box
     [tooltip (txt-c :back)
      [button {:on-click #(>evt :page/pop)}
       [arrow-back]]]]))
