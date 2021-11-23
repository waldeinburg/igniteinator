(ns igniteinator.ui.header
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.constants :as const]
            [igniteinator.ui.hooks :refer [desktop-menu?-hook show-title-in-bar?-hook]]
            [igniteinator.ui.components.menu-button :refer [menu-button]]
            [igniteinator.ui.components.link :refer [external-link]]
            [igniteinator.ui.singletons.share-button :refer [share-button]]
            [igniteinator.ui.singletons.install-button :refer [install-button]]
            [igniteinator.ui.singletons.language-menu :refer [language-menu]]
            [igniteinator.ui.settings.settings-menu :refer [settings-button]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.material.app-bar :refer [app-bar]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.icons.menu :refer [menu] :rename {menu menu-icon}]
            [reagent-mui.material.menu-item :refer [menu-item]]))

(def main-menu-list [[:cards :cards-page-title]
                     [:setups :setups-page-title]])

(defn navigate [page-key]
  (>evt :page/set page-key))

(defn if-desktop-menu [f]
  [:f> (fn []
         (if (desktop-menu?-hook)
           f))])

(defn if-mobile-menu [f]
  [:f> (fn []
         (if (not (desktop-menu?-hook))
           f))])

(defn main-menu-items [current-page item-fn]
  (map (fn [[page-key title-key]]
         (let [active? (= current-page page-key)]
           (item-fn page-key (txt title-key) active?)))
    main-menu-list))

(defn title []
  [box
   [typography {:component "h1", :variant "h4"} "Igniteinator"]
   [:div.subtitle "– " (txt :subtitle) " " [external-link const/ignite-link "Ignite"]]])

(defn bar-title []
  [:f>
   (fn []
     (if (show-title-in-bar?-hook)
       [title]))])

(defn mobile-menu-title []
  [:f>
   (fn []
     (if (not (show-title-in-bar?-hook))
       [box {:px 2, :pb 2}
        [title]]))])

(defn main-menu-mobile []
  (let [current-page (<sub :current-page)
        on-close     #(>evt :main-menu-mobile/set-open? false)]
    [box {:mr 2}
     [menu-button {:button-id     :main-menu-button
                   :menu-id       :main-menu-mobile
                   :open?-sub     :main-menu-mobile/open?
                   :set-open?-evt :main-menu-mobile/set-open?
                   :tooltip-key   :main-menu-button-tooltip
                   :button-elem   button
                   :icon          menu-icon
                   :button-props  {:color   :primary
                                   :variant :outlined}}
      [mobile-menu-title]
      (add-children
        (main-menu-items current-page
          (fn [page-key title active?]
            [menu-item {:selected active?
                        :on-click (if active?
                                    on-close
                                    (fn []
                                      (on-close)
                                      (navigate page-key)))}
             title])))]]))

(defn main-menu-desktop []
  (let [current-page (<sub :current-page)]
    [box {:ml 2, :component "nav", :role :menubar}
     (add-children
       (main-menu-items current-page
         (fn [page-key title active?]
           [button {:role     :menuitem,
                    :color    (if active?
                                :secondary
                                :primary)
                    :on-click (if (not active?)
                                #(navigate page-key))}
            title])))]))

(defn header []
  [:header
   [app-bar {:position :static, :color :transparent}
    [toolbar {:display :flex}
     [if-mobile-menu [main-menu-mobile]]
     [bar-title]
     [if-desktop-menu [main-menu-desktop]]
     [box {:ml "auto"}
      [share-button]
      [language-menu]
      [settings-button]
      [install-button]]]]])
