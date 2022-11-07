(ns igniteinator.ui.header
  (:require [igniteinator.constants :as const]
            [igniteinator.router :refer [resolve-to-href]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.link :refer [external-link internal-link]]
            [igniteinator.ui.components.menu-button :refer [menu-button]]
            [igniteinator.ui.hooks :refer [desktop-menu?-hook show-title-in-bar?-hook]]
            [igniteinator.ui.settings.settings-menu :refer [settings-button]]
            [igniteinator.ui.singletons.install-button :refer [install-button]]
            [igniteinator.ui.singletons.language-menu :refer [language-menu]]
            [igniteinator.ui.singletons.share-button :refer [share-button]]
            [igniteinator.util.event :refer [blur link-on-click]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [reagent-mui.icons.menu :refer [menu] :rename {menu menu-icon}]
            [reagent-mui.material.app-bar :refer [app-bar]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.menu-item :refer [menu-item]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.typography :refer [typography]]))

(def main-menu-list [[:cards :cards-page-title]
                     [:setups :setups-page-title]
                     [:randomizer :randomizer/page-title]
                     [:epic :epic/page-title]])

(defn navigate-fn [page-key pre-navigate]
  (link-on-click (fn [event]
                   (blur event)
                   (if pre-navigate
                     (pre-navigate))
                   (>evt :page/navigate page-key))))

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

(defn front-link []
  [internal-link :front
   {:color     :black
    :underline :none}
   "Igniteinator"])

(defn title []
  [box
   [typography {:component "h1", :variant "h4"}
    [front-link]]
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
            [menu-item {:component :a
                        :selected  active?
                        :href      (resolve-to-href page-key)
                        :on-click  (navigate-fn page-key on-close)}
             title])))]]))

(defn main-menu-desktop []
  (let [current-page (<sub :current-page)]
    [box {:ml 2, :component "nav", :role :menubar}
     (add-children
       (main-menu-items current-page
         (fn [page-key title active?]
           [button {:href     (resolve-to-href page-key)
                    :role     :menuitem,
                    :color    (if active?
                                :secondary
                                :primary)
                    :on-click (navigate-fn page-key nil)}
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
