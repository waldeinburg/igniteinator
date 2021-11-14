(ns igniteinator.ui.settings.size
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.text :refer [txt]]
            [reagent-material-ui.core.form-control :refer [form-control]]
            [reagent-material-ui.core.form-label :refer [form-label]]
            [reagent-material-ui.core.slider :refer [slider]]))

(defn mark [value label-key]
  {:value value, :label (txt label-key)})

(defn size-settings []
  [form-control {:component  "fieldset"
                 :class-name :full-width}
   [form-label {:component "legend"} (txt :settings.size/label)]
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
