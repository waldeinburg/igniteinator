(ns igniteinator.ui.singletons.install-button
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.util.environment :refer [user-agent]]
            [reagent.core :as r]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.icons.add-to-home-screen :refer [add-to-home-screen]]
            [reagent-mui.material.dialog :refer [dialog]]
            [reagent-mui.material.dialog-title :refer [dialog-title]]
            [reagent-mui.material.dialog-content :refer [dialog-content]]
            [reagent-mui.material.dialog-actions :refer [dialog-actions]]
            [reagent-mui.material.button :refer [button]]))

(defonce beforeinstallprompt-event (r/atom nil))

(defn reg-beforeinstallprompt-event []
  (.addEventListener js/self "beforeinstallprompt"
    (fn [e]
      (js/console.log "Received beforeinstallprompt event")
      ;; Don't show prompt automatically.
      (.preventDefault e)
      (reset! beforeinstallprompt-event e))))

(defn handle-install [e-atom]
  (let [e @e-atom]
    (.prompt e)
    (.then (.-userChoice e)
      (fn [res]
        (when (= "accepted" (.-outcome res))
          (reset! e-atom nil))))))

(defn- install-button-fn [on-click color]
  [tooltip (txt :install-app)
   [icon-button {:on-click on-click}
    [add-to-home-screen {:color color}]]])

(defn on-install-dialog-close []
  (>evt :install-dialog/set-open? false))

(defn add-to-home-screen-dialog []
  [dialog {:open     (<sub :install-dialog/open?)
           :on-close on-install-dialog-close}
   [dialog-title (txt :a2hs-instructions-title)]
   [dialog-content
    [:p (txt (condp = (user-agent)
               :ios :a2hs-instructions-ios
               nil))]
    [dialog-actions
     [button {:variant :contained, :on-click on-install-dialog-close} (txt :got-it)]]]])

(defn install-button []
  [:<>
   [add-to-home-screen-dialog]
   (if @beforeinstallprompt-event
     ;; Browser supports install prompt.
     [install-button-fn #(handle-install beforeinstallprompt-event) :primary]
     ;; No install prompt event. See if we can help the user.
     (when (and
             (not (.. js/self -navigator -standalone))
             ;; Just one known for now
             (#{:ios} (user-agent)))
       [install-button-fn #(>evt :install-dialog/set-open? true) :inherit]))])
