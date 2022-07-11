(ns igniteinator.epic.trash
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.material.button :refer [button]]
            [reagent.core :as r]
            [reagent-mui.icons.delete-forever :refer [delete-forever]]))

(defn trash-button []
  (let [setup (<sub :epic/setup)]
    (if (:trash-to-bottom? setup)
      [button {:variant    :outlined
               :sx         {:mr 2}
               :start-icon (r/as-element [delete-forever])}
       "Trash"])))
