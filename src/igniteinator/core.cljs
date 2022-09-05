(ns ^:figwheel-hooks igniteinator.core
  (:require-macros [igniteinator.util.debug :refer [when-debug when-dev]])
  (:require
    [day8.re-frame.http-fx]                                 ; Re-frame registrations
    [goog.dom :as gdom]
    [igniteinator.cofx]                                     ; Re-frame registrations
    [igniteinator.constants :as constants]
    [igniteinator.events]                                   ; Re-frame registrations
    [igniteinator.fx]                                       ; Re-frame registrations
    [igniteinator.service-worker-client :refer [reg-sw]]
    [igniteinator.subs]                                     ; Re-frame registrations
    [igniteinator.ui.app :refer [app]]
    [igniteinator.ui.singletons.install-button :refer [reg-beforeinstallprompt-event]]
    [igniteinator.util.re-frame :refer [>evt]]
    [re-frame.core :as rf]
    [reagent.dom :as rdom]))

(defonce placeholder-img-cache (js/Image.))

(defn preload-placeholder-img []
  ;; Preloading the placeholder image is essential when starting on a navigation page. Without it, the swipeable-view
  ;; element will receive a child with img-elements with a height of 0 because the placeholder image is not yet loaded.
  ;; This will give a page with only a tiny part of the content visible until navigating to the next slide.
  ;; Just creating the Image in an effect triggered proved effective only when starting on a card display. When
  ;; starting on the front page and navigating to a suggested setup, the image had been garbage collected. At least it
  ;; is my and the fact that the following works seems to prove it.
  (set! (. placeholder-img-cache -src) constants/placeholder-img-src))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [app] el))

(defn init [app-element]
  (preload-placeholder-img)
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
