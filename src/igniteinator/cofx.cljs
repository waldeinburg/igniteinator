(ns igniteinator.cofx
  (:require [igniteinator.util.environment :as environment]
            [re-frame.core :refer [reg-cofx]]
            [cljs-http.client :refer [parse-url]]))

(reg-cofx
  :scroll-top
  (fn [cofx _]
    (assoc cofx :scroll-top
                (max
                  ;; Safari
                  (.. js/document -body -scrollTop)
                  ;; Others
                  (.. js/document -documentElement -scrollTop)))))

(reg-cofx
  :query-params
  (fn [cofx _]
    (assoc cofx :query-params
                (-> js/self .-location.href parse-url :query-params))))

(reg-cofx :standalone-mode?
  (fn [cofx _]
    (assoc cofx :standalone-mode? (environment/standalone-mode?))))
