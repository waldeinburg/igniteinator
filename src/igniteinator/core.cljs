(ns ^:figwheel-hooks igniteinator.core
  (:require
    ;; Re-frame registrations BEGIN
    [igniteinator.events]
    [igniteinator.subs]
    [igniteinator.fx]
    [igniteinator.cofx]
    [day8.re-frame.http-fx]
    ;; Re-frame registrations END
    [igniteinator.ui.app :refer [app]]
    [igniteinator.util.re-frame :refer [>evt]]
    [igniteinator.util.message :as msg]
    [igniteinator.service-worker-client :refer [reg-sw]]
    [igniteinator.ui.singletons.install-button :refer [reg-beforeinstallprompt-event]]
    [goog.dom :as gdom]
    [re-frame.core :as rf]
    [reagent.dom :as rdom])
  (:require-macros [igniteinator.util.debug :refer [when-debug when-dev]]))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [app] el))

(defn init [app-element]
  (rf/dispatch-sync [:init-db])
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
