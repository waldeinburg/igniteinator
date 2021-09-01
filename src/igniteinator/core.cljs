(ns ^:figwheel-hooks igniteinator.core
  (:require
    [igniteinator.ui.main :refer [main]]
    [igniteinator.util.message :as msg]
    [igniteinator.service-worker-client :refer [reg-sw]]
    [igniteinator.ui.install-button :refer [reg-beforeinstallprompt-event]]
    [igniteinator.data-load :refer [load-data]]
    [igniteinator.state :refer [state]]
    [goog.dom :as gdom]
    [reagent.dom :as rdom]))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [main] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (load-data)
    (reg-sw)
    (reg-beforeinstallprompt-event)
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

(defn ^:before-load my-before-reload-callback []
  ;; Tell the service worker, if any, to clear app cache before Figwheel reload.
  (if-let [ctrl (.-controller js/navigator.serviceWorker)]
    (msg/post ctrl :cache-clear)))

(defn ^:after-load on-reload []
  (mount-app-element))
