(ns igniteinator.ui.header
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.ui.link :refer [external-link]]
            [igniteinator.constants :as const]
            [igniteinator.ui.install-button :refer [install-button]]
            [goog.dom :as gdom]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.typography :refer [typography]]
            [reagent-material-ui.core.app-bar :refer [app-bar]]
            [reagent-material-ui.core.toolbar :refer [toolbar]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.icons.menu :refer [menu] :rename {menu menu-icon}]
            [reagent-material-ui.core.menu :refer [menu]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.core.use-media-query :refer [use-media-query]]))

(def main-menu-list [[:cards :cards-page-title]
                     [:setups :setups-page-title]])

(defn desktop-menu?-hook []
  (use-media-query "(min-width:650px)"))

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

(defn main-menu-mobile []
  (let [current-page (<sub :current-page)
        open?        (<sub :main-menu-mobile/open?)
        on-close     #(>evt :main-menu-mobile/set-open? false)]
    [box {:mr 2}
     [icon-button {:id            :main-menu-button
                   :aria-controls :main-menu-mobile
                   :aria-haspopup true
                   :aria-expanded open?
                   :on-click      #(>evt :main-menu-mobile/set-open? true)}
      [menu-icon]]
     [menu {:id            :main-menu-mobile
            :open          open?
            :on-close      on-close
            :anchor-el     #(gdom/getElement (name :main-menu-button))
            :MenuListProps {:aria-labelledby :main-menu-button}}
      (add-children
        (main-menu-items current-page
          (fn [page-key title active?]
            [menu-item {:selected active?
                        :on-click (if active?
                                    on-close
                                    (fn []
                                      (on-close)
                                      (>evt :page/push page-key)))}
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
                                #(>evt :page/push page-key))}
            title])))]))

(defn header []
  [:header
   [app-bar {:position :static, :color :transparent}
    [toolbar {:display :flex}
     [if-mobile-menu [main-menu-mobile]]
     [title]
     [if-desktop-menu [main-menu-desktop]]
     [box {:ml "auto"}
      [install-button]]]]])
