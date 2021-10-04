(ns igniteinator.fx
  (:require [re-frame.core :refer [reg-fx]]))

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
  (fn []
    (.. js/window -location reload)))
