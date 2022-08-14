(ns ^:figwheel-hooks igniteinator.core
  (:require-macros [igniteinator.util.debug :refer [when-debug when-dev]])
  (:require
    ;; Re-frame registrations BEGIN
    [day8.re-frame.http-fx]
    [goog.dom :as gdom]
    [igniteinator.cofx]
    [igniteinator.events]
    [igniteinator.fx]
    ;; Re-frame registrations END
    [igniteinator.service-worker-client :refer [reg-sw]]
    [igniteinator.subs]
    [igniteinator.ui.app :refer [app]]
    [igniteinator.ui.singletons.install-button :refer [reg-beforeinstallprompt-event]]
    [igniteinator.util.re-frame :refer [>evt]]
    [re-frame.core :as rf]
    [reagent.dom :as rdom]))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [app] el))

(defn init [app-element]
  (rf/dispatch-sync [:init-db])
  (rf/dispatch-sync [:router/start])
  (>evt :load-data)
  (reg-sw)
  (reg-beforeinstallprompt-event)
  (mount app-element))

(defn start-app []
  (js/console.log "Starting app")
  (when-dev
    (js/console.log "Dev mode"))
  (when-debug
    (js/console.log "Debug mode"))
  (if-let [el (get-app-element)]
    (init el)
    (js/document.write "Fatal Error: No app element!")))

(defn ^:after-load after-reload []
  (rf/clear-subscription-cache!)
  (start-app))

;; Entry-point called from index.html. This way mount-app-element is called on load and only once on reload (in the
;; figwheel-hook).
(defn ^:export main []
  (start-app))
