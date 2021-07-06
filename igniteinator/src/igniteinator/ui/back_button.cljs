(ns igniteinator.ui.back-button
  (:require [igniteinator.state :refer [state set-state!]]
            [igniteinator.text :refer [txt]]
            [reagent.core :as r]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.tooltip :refer [tooltip]]
            [reagent-material-ui.icons.arrow-back :refer [arrow-back]]))

(let [previous-page (r/cursor state [:previous-page])]
  (defn back-button []
    [box
     [tooltip {:title (txt :back)}
      [button {:on-click #(set-state!
                            :current-page @previous-page
                            :previous-page nil)}
       [arrow-back]]]]))
