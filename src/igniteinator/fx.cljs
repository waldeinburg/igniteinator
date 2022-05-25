(ns igniteinator.fx
  (:require [igniteinator.util.message :as msg]
            [igniteinator.util.re-frame :refer [>evt]]
            [re-frame.core :refer [reg-fx]]
            [akiroz.re-frame.storage :as storage]
            [promesa.core :as p])
  (:require-macros [igniteinator.util.debug :refer [dbg]]))

(defn- reload []
  (.. js/window -location reload))

(defn- scroll-to [n]
  ;; Safari
  (set! (.. js/document -body -scrollTop) n)
  ;; Others
  (set! (.. js/document -documentElement -scrollTop) n))

(storage/reg-co-fx!
  :igniteinator/store
  {:fx   :store
   :cofx :store})

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
    (dbg "Post message fx" (clj->js msg-type))
    (if-let [sw-cnt js/navigator.serviceWorker]
      ;; Be sure the message is not lost because of race conditions.
      (p/let [sw-reg (.-ready sw-cnt)]
        (msg/post (.-active sw-reg) msg-type msg-data))
      (js/console.warn "navigator.serviceWorker not available"))))

(reg-fx
  :epic/shuffle-stacks
  (fn [stacks]
    (let [shuffled-stacks (mapv stacks #(update % :cards shuffle))]
      (>evt :epic/set-stacks shuffled-stacks))))
