(ns igniteinator.ui.settings.size
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [reagent-material-ui.core.slider :refer [slider]]))

(defn mark [value label-key]
  {:value value, :label (txt label-key)})

(defn size-settings []
  [form-item {:label      (txt :settings.size/label)
              :class-name :full-width}
   [slider {:value               (<sub :size)
            :value-label-display :off
            :track               false
            :step                nil
            :min                 0
            :max                 3
            :on-change           #(>evt :set-size %2)
            :marks               [(mark 0 :settings.size/size-0)
                                  (mark 1 :settings.size/size-1)
                                  (mark 2 :settings.size/size-2)
                                  (mark 3 :settings.size/size-3)]}]])
