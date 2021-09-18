(ns ^:figwheel-hooks igniteinator.core
  (:require
    ;; Re-frame registrations BEGIN
    [igniteinator.events]
    [igniteinator.subs]
    [day8.re-frame.http-fx]
    ;; Re-frame registrations END
    [igniteinator.ui.app :refer [app]]
    [igniteinator.util.re-frame :refer [>evt]]
    [igniteinator.util.message :as msg]
    [igniteinator.service-worker-client :refer [reg-sw]]
    [igniteinator.ui.install-button :refer [reg-beforeinstallprompt-event]]
    [goog.dom :as gdom]
    [re-frame.core :as rf]
    [reagent.dom :as rdom]))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [app] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (rf/dispatch-sync [:init-db])
    (>evt :load-data)
    (reg-sw)
    (reg-beforeinstallprompt-event)
    (mount el)))

(defn ^:before-load before-reload []
  ;; Tell the service worker, if any, to clear app cache before Figwheel reload.
  (if-let [ctrl (.-controller js/navigator.serviceWorker)]
    (msg/post ctrl :cache-clear)))

(defn ^:after-load after-reload []
  (rf/clear-subscription-cache!)
  (mount-app-element))

;; Entry-point called from index.html. This way mount-app-element is called on load and only once on reload (in the
;; figwheel-hook).
(defn ^:export main []
  (mount-app-element))
