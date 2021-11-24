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
       [alert {:severity (if finished? :success :info)
               :variant  (if finished? :filled :standard)
               :icon     false
               ;; This doesn't work on desktop for some reason, but the download progress is aimed at people installing
               ;; as a smartphone app anyway.
               :sx       {:width "100%"}}
        [alert-title (txt :caching-progress-title)]
        [linear-progress {:variant :determinate
                          :value   value
                          :sx      {:height 10, :border-radius 5}}]]])))
