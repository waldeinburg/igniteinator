(ns ^:figwheel-hooks igniteinator.core
  (:require
    [goog.dom :as gdom]
    [reagent.dom :as rdom]
    [igniteinator.ui.main :refer [main]]
    [igniteinator.data-load :refer [load-data]]
    [igniteinator.state :refer [state]]))

(defn get-app-element []
  (gdom/getElement "app"))

(defn reg-sw []
  (if js/navigator.serviceWorker
    (->
      (.register js/navigator.serviceWorker "/sw.js")
      (.then #(js/console.log "Service worker installed"))
      (.catch #(js/console.error "Failed to load service worker:" %)))
    (js/console.log "navigator.serviceWorker not available")))

(defn mount [el]
  (rdom/render [main] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (load-data)
    (reg-sw)
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
