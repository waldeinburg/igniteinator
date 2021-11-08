(ns igniteinator.service-worker-client
  (:require [igniteinator.util.re-frame :refer [>evt]]
            [igniteinator.util.message :as msg]
            [igniteinator.ui.caching-progress :refer [handle-img-cache-message]]
            [promesa.core :as p]))

(def handle-message (msg/message-handler
                      (fn [msg-type msg-data _]
                        (condp = msg-type
                          :img-caching-progress (handle-img-cache-message msg-data)
                          (js/console.warn "Invalid message type" (if (keyword? msg-type)
                                                                    (name msg-type)
                                                                    msg-type))))))

(defn handle-service-worker-registered [sw-reg]
  (js/console.log "Service worker registered")
  ;; Register update event listener, but only if there's already a service worker. We don't want a notification the
  ;; first time we visit the page.
  (if (.-active sw-reg)
    (.addEventListener sw-reg "updatefound"
      (fn []
        (let [new-sw (.-installing sw-reg)]
          (.addEventListener new-sw "statechange"
            (fn []
              ;; Do not skip this step. If installation goes wrong we don't want to fire an update notification.
              (if (= "installed" (.-state new-sw))
                (>evt :update-available new-sw)))))))))

(defn setup-sw [sw-cnt]
  (->
    (.register sw-cnt "/sw.js")
    (p/then handle-service-worker-registered)
    (p/catch #(js/console.error "Failed to load service worker:" %)))
  (.addEventListener sw-cnt "message" handle-message)
  ;; Redundant to wait and send service worker. But then the fx won't break if not using ready().
  (p/let [sw-reg (.-ready sw-cnt)]
    (>evt :service-worker-ready sw-reg)))

(defn reg-sw []
  (if-let [sw-cnt js/navigator.serviceWorker]
    (setup-sw sw-cnt)
    (js/console.log "navigator.serviceWorker not available")))
