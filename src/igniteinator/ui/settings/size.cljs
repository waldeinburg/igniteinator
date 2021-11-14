(ns igniteinator.ui.settings.size
  (:require [igniteinator.util.re-frame :refer [<sub-ref >evt]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.radio-group :refer [radio-group radio]]))

(defn size-settings []
  [radio-group {:label      (txt :settings.size/label)
                :row        true
                :value-ref  (<sub-ref :size)
                :value-type :number
                :on-change  #(>evt :set-size %)}
   [radio {:value 0, :label (txt :settings.size/size-0)}]
   [radio {:value 1, :label (txt :settings.size/size-1)}]
   [radio {:value 2, :label (txt :settings.size/size-2)}]
   [radio {:value 3, :label (txt :settings.size/size-3)}]])
