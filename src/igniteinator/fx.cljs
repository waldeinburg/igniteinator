(ns igniteinator.fx
  (:require-macros [igniteinator.util.debug :refer [dbg]])
  (:require [akiroz.re-frame.storage :as storage]
            [igniteinator.router :as router]
            [igniteinator.util.message :as msg]
            [igniteinator.util.re-frame :refer [>evt]]
            [promesa.core :as p]
            [re-frame.core :refer [reg-fx]]
            [reagent.core :as r]))

(defn- reload []
  (.. js/window -location reload))

(defn- scroll-to [n]
  ;; Used after page shift. Important that the DOM is ready.
  (r/after-render
    (fn []
      ;; Safari
      (set! (.. js/document -body -scrollTop) n)
      ;; Others
      (set! (.. js/document -documentElement -scrollTop) n))))

(storage/reg-co-fx!
  :igniteinator/store
  {:fx   :store
   :cofx :store})

(reg-fx
  :router/start
  router/start!)

(reg-fx
  :router/navigate
  (fn [[id params query]]
    (router/navigate! id params query)))

(reg-fx
  :router/replace
  (fn [[id params query]]
    (router/replace! id params query)))

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
    (let [shuffled-stacks (mapv #(update % :cards shuffle) stacks)]
      (>evt :epic/shuffled-stacks shuffled-stacks))))
