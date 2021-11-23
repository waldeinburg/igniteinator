(ns igniteinator.ui.singletons.caching-progress
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.text :refer [txt]]
            [reagent-mui.material.snackbar :refer [snackbar]]
            [reagent-mui.material.alert :refer [alert]]
            [reagent-mui.material.alert-title :refer [alert-title]]
            [reagent-mui.material.linear-progress :refer [linear-progress]]))

(defn handle-img-cache-message [msg-data]
  ;; The message data will contain a progress and a count integer.
  ;; Also, set an open flag (cf. the compenent below).
  (>evt :caching-progress/set-progress msg-data))

#_(def thick-linear-progress
  ((styles/with-styles {:root {:height        10
                               :border-radius 5}})
   linear-progress))
(def thick-linear-progress linear-progress)
#_(def large-alert ((styles/with-styles {:root    {:width "100%"}
                                       :message {:width "100%"}})
                  alert))
(def large-alert alert)

(defn caching-progress []
  ;; If we never got a message about caching progress, avoid rendering at all. But when rendering, we want to show/hide
  ;; based on the :open boolean to get the transition animation.
  (when (<sub :caching-progress/initiated?)
    (let [count     (<sub :caching-progress/count)
          progress  (<sub :caching-progress/progress)
          c         (if (zero? count) 1 count)              ; avoid division by zero
          value     (* 100 (/ progress c))
          finished? (= 100 value)]
      [snackbar {:anchor-origin           {:vertical :bottom, :horizontal :center}
                 :open                    (<sub :caching-progress/open?)
                 :auto-hide-duration      (if finished? 1000)
                 :on-close                #(>evt :caching-progress/set-open? false)
                 "ClickAwayListenerProps" {"mouseEvent" false
                                           "touchEvent" false}}
       [large-alert {:severity (if finished? :success :info)
                     :variant  (if finished? :filled :standard)
                     :icon     false}
        [alert-title (txt :caching-progress-title)]
        [thick-linear-progress {:variant :determinate
                                :value   value}]]])))
