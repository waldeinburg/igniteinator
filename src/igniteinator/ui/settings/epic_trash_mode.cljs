(ns igniteinator.ui.settings.epic-trash-mode
  (:require [igniteinator.ui.components.radio-group :refer [radio radio-group]]
            [igniteinator.util.re-frame :refer [<sub-ref >evt]]))

(defn epic-trash-mode-setting []
  [radio-group {:value-ref (<sub-ref :epic/trash-mode)
                :on-change #(>evt :epic/set-trash-mode %)
                :label     "Epic Ignite: Trash mode"}
   [radio {:value :page
           :label "Select from images"}]
   [radio {:value :dialog
           :label "Select from titles"}]])
