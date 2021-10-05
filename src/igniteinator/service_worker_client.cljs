(ns igniteinator.service-worker-client
  (:require [igniteinator.util.re-frame :refer [>evt]]
            [igniteinator.util.message :as msg]
            [igniteinator.ui.caching-progress :refer [handle-img-cache-message]]
            [promesa.core :as p]))

(defn notify-update-event [{:keys [version]}]
  (>evt :notify-update version))

(defn standalone-mode? []
  (or
    (.. js/self -navigator -standalone)
    (-> js/self (.matchMedia "(display-mode: standalone)") .-matches)))

(def handle-message (msg/message-handler
                      (fn [msg-type msg-data _]
                        (condp = msg-type
                          :img-caching-progress (handle-img-cache-message msg-data)
                          :update (notify-update-event msg-data)
                          (js/console.warn "Invalid message type" (if (keyword? msg-type)
                                                                    (name msg-type)
                                                                    msg-type))))))

(defn post-skip-waiting-if-waiting [sw-registration]
  (if-let [sw (.-waiting sw-registration)]
    (msg/post sw :skip-waiting nil)))

(defn setup-sw [sw-cnt]
  (->
    (.register sw-cnt "/sw.js")
    (p/then (fn [sw-reg]
              (js/console.log "Service worker registered" sw-reg)
              ;; Post to the waiting service worker that we want it to skip waiting. It will respond with a message
              ;; telling the version and the handler will show an update notification.
              (let [post-skip-w #(post-skip-waiting-if-waiting sw-reg)]
                (.addEventListener js/self "waiting" post-skip-w)
                (.addEventListener js/self "externalwaiting" post-skip-w)
                ;; The waiting property will be null initially.
                (post-skip-w))))
    (p/catch #(js/console.error "Failed to load service worker:" %)))
  (.addEventListener sw-cnt "message" handle-message)
  (p/let [sw-reg (.-ready sw-cnt)]
    (if (standalone-mode?)
      (msg/post (.-active sw-reg) :mode :standalone))))

(defn reg-sw []
  (if-let [sw-cnt js/navigator.serviceWorker]
    (setup-sw sw-cnt)
    (js/console.log "navigator.serviceWorker not available")))
