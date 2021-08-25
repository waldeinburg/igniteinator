(ns igniteinator.service-worker-client)

(defn standalone-mode? []
  (or
    (.. js/self -navigator -standalone)
    (->
      (.matchMedia js/self "(display-mode: standalone)")
      .-matches)))

(defn setup-sw []
  (->
    (.register js/navigator.serviceWorker "/sw.js")
    (.then #(js/console.log "Service worker registered"))
    (.catch #(js/console.error "Failed to load service worker:" %)))

  (->
    js/navigator.serviceWorker
    .-ready
    (.then (fn [sw-reg]
             (if (standalone-mode?)
               (->
                 sw-reg
                 .-active
                 (.postMessage (clj->js {:type :mode, :value :standalone}))))))))

(defn reg-sw []
  (if js/navigator.serviceWorker
    (setup-sw)
    (js/console.log "navigator.serviceWorker not available")))
