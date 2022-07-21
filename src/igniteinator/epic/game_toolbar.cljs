(ns igniteinator.epic.game-toolbar
  (:require [igniteinator.epic.trash :refer [trash-button]]
            [igniteinator.util.re-frame :refer [<sub]]
            [reagent-mui.material.snackbar :refer [snackbar]]
            [reagent.core :as r]))

(defn game-snackbar [open?-sub message-sub]
  [snackbar
   {:open    (<sub open?-sub)
    :message (r/as-element [:span (<sub message-sub)])}])

(defn game-toolbar []
  (let [trash-to-bottom? (<sub :epic/trash-to-bottom?)]
    [:<>
     [game-snackbar :epic/snackbar-1-open? :epic/snackbar-1-message]
     [game-snackbar :epic/snackbar-2-open? :epic/snackbar-2-message]
     (if trash-to-bottom?
       [trash-button])]))
