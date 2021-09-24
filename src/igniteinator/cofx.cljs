(ns igniteinator.cofx
  (:require [re-frame.core :refer [reg-cofx]]))

(reg-cofx
  :scroll-top
  (fn [cofx _]
    (assoc cofx :scroll-top
                (max
                  ;; Safari
                  (.. js/document -body -scrollTop)
                  ;; Others
                  (.. js/document -documentElement -scrollTop)))))
