(ns igniteinator.fx
  (:require [re-frame.core :refer [reg-fx]]))

(reg-fx
  :scroll-to-top
  (fn []
    ;; Safari
    (set! (.. js/document -body -scrollTop) 0)
    ;; Others
    (set! (.. js/document -documentElement -scrollTop) 0)))
