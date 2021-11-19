(ns igniteinator.ui.settings.display-name
  (:require [igniteinator.util.re-frame :refer [<sub-ref >evt]]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.radio-group :refer [radio-group radio]]))

(defn display-name-settings []
  [radio-group {:value-ref (<sub-ref :display-name?-setting)
                :on-change #(>evt :set-display-name? %)
                :label     (txt :settings.display-name/label)}
   [radio {:value :always
           :label (txt-c :always)}]
   [radio {:value :never
           :label (txt-c :never)}]
   [radio {:value :translating
           :label (txt :settings.display-name/translating)}]])
