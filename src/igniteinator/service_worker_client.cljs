(ns igniteinator.service-worker-client
  (:require [igniteinator.util.message :as msg]
            [igniteinator.ui.caching-progress :refer [handle-img-cache-message]]
            [promesa.core :as p]))

(defn standalone-mode? []
  (or
    (.. js/self -navigator -standalone)
    (.. (.matchMedia js/self "(display-mode: standalone)") -matches)))

(def handle-message (msg/message-handler
                      (fn [msg-type msg-data _]
                        (condp = msg-type
                          :img-caching-progress (handle-img-cache-message msg-data)
                          (js/console.warn "Invalid message type" (name msg-type))))))

(defn setup-sw [sw-cnt]
  (->
    (.register sw-cnt "/sw.js")
    (p/then #(js/console.log "Service worker registered"))
    (p/catch #(js/console.error "Failed to load service worker:" %)))
  (.addEventListener sw-cnt "message" handle-message)
  (p/let [sw-reg (.-ready sw-cnt)]
    (if (standalone-mode?)
      (->
        sw-reg
        .-active
        (msg/post :mode :standalone)))))

(defn reg-sw []
  (if-let [sw-cnt js/navigator.serviceWorker]
    (setup-sw sw-cnt)
    (js/console.log "navigator.serviceWorker not available")))
