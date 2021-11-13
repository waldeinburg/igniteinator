(ns igniteinator.ui.components.menu-button
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [goog.dom :as gdom]
            [reagent-material-ui.core.menu :refer [menu]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]))

(defn menu-button [{:keys [button-id menu-id tooltip-key icon open?-sub set-open?-evt]}
                   & children]
  (let [open? (<sub open?-sub)]
    [:<>
     [tooltip (txt tooltip-key)
      [icon-button {:id            button-id
                    :aria-controls menu-id
                    :aria-haspopup true
                    :aria-expanded open?
                    :on-click      #(>evt set-open?-evt true)}
       [icon]]]
     [menu {:id            menu-id
            :open          open?
            :on-close      #(>evt set-open?-evt false)
            :anchor-el     #(gdom/getElement (name button-id))
            :MenuListProps {:aria-labelledby button-id}}
      (add-children children)]]))
