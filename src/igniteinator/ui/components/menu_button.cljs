(ns igniteinator.ui.components.menu-button
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [goog.dom :as gdom]
            [reagent-mui.material.menu :refer [menu]]
            [reagent-mui.material.icon-button :refer [icon-button]]))

(defn menu-button [{:keys [button-elem
                           button-id menu-id tooltip-key icon
                           open?-sub set-open?-evt
                           button-props
                           menu-props]
                    :or   {button-elem icon-button}}
                   & children]
  (let [open? (<sub open?-sub)]
    [:<>
     [tooltip (txt tooltip-key)
      [button-elem (into {:id            button-id
                          :aria-controls menu-id
                          :aria-haspopup true
                          :aria-expanded open?
                          :on-click      #(>evt set-open?-evt true)}
                     button-props)
       [icon]]]
     [menu (into {:id            menu-id
                  :open          open?
                  :on-close      #(>evt set-open?-evt false)
                  :anchor-el     #(gdom/getElement (name button-id))
                  :MenuListProps {:aria-labelledby button-id}}
             menu-props)
      (add-children children)]]))
