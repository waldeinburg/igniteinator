(ns igniteinator.ui.header
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.ui.link :refer [external-link]]
            [igniteinator.constants :as const]
            [igniteinator.ui.install-button :refer [install-button]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.app-bar :refer [app-bar]]
            [reagent-material-ui.core.toolbar :refer [toolbar]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.typography :refer [typography]]))

(defn main-menu-items [current-page]
  (map (fn [[page-key title-key]]
         (let [active? (= current-page page-key)]
           [button {:role     :menuitem,
                    :color    (if active?
                                :secondary
                                :primary)
                    :on-click (if (not active?)
                                #(>evt :page/push page-key))}
            (txt title-key)]))
    [[:cards :cards-page-title]
     [:setups :setups-page-title]]))

(defn main-menu []
  (let [current-page (<sub :current-page)]
    [box {:ml 2, :component "nav", :role :menubar}
     (add-children (main-menu-items current-page))]))

(defn header []
  [:header
   [app-bar {:position :static, :color :transparent}
    [toolbar {:display :flex}
     [box
      [typography {:component "h1", :variant "h4"} "Igniteinator"]
      [:div.subtitle "– " (txt :subtitle) " " [external-link const/ignite-link "Ignite"]]]
     [main-menu]
     [box {:ml "auto"}
      [install-button]]]]])
