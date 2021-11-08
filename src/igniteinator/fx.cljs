(ns igniteinator.fx
  (:require [igniteinator.util.message :as msg]
            [igniteinator.util.environment :as environment]
            [re-frame.core :refer [reg-fx]]
            [promesa.core :as p]))

(defn- reload []
  (.. js/window -location reload))

(defn- scroll-to [n]
  ;; Safari
  (set! (.. js/document -body -scrollTop) n)
  ;; Others
  (set! (.. js/document -documentElement -scrollTop) n))

(reg-fx
  :scroll-to
  scroll-to)

(reg-fx
  :scroll-to-top
  (fn []
    (scroll-to 0)))

(reg-fx
  :reload
  reload)

(reg-fx
  :update-app
  (fn [new-sw]
    ;; Reload when the new service worker is ready to take over.
    (.addEventListener js/navigator.serviceWorker "controllerchange" reload)
    ;; Tell the new service worker to activate immediately.
    (msg/post new-sw :skip-waiting nil)))

(reg-fx
  :post-message
  (fn [[msg-type msg-data]]
    (if-let [sw-cnt js/navigator.serviceWorker]
      ;; Be sure the message is not lost because of race conditions.
      (p/let [sw-reg (.-ready sw-cnt)]
        (if (environment/standalone-mode?)
          (msg/post (.-active sw-reg) msg-type msg-data))))))
