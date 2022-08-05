(ns igniteinator.epic.game-toolbar
  (:require [clojure.string :as s]
            [igniteinator.epic.trash :refer [trash-button]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent-mui.icons.redo :refer [redo] :rename {redo redo-icon}]
            [reagent-mui.icons.undo :refer [undo] :rename {undo undo-icon}]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.material.snackbar :refer [snackbar]]
            [reagent.core :as r]))

(defn history-button [on-click-evt-key title-sub-key verb icon]
  (let [title (<sub title-sub-key)
        btn   [icon-button {:color    :inherit
                            :on-click #(>evt on-click-evt-key)
                            :disabled (not title)}
               [icon]]]
    (if title
      [tooltip (str (s/capitalize verb) " " title) btn]
      btn)))

(defn game-snackbar [open?-sub message-sub]
  [snackbar
   {:open    (<sub open?-sub)
    :message (r/as-element
               [:<>
                [box {:component :span, :mr 2}
                 [history-button :epic/undo :epic/undo-title "undo" undo-icon]
                 [history-button :epic/redo :epic/redo-title "redo" redo-icon]]
                [:span (<sub message-sub)]])}])

(defn game-toolbar []
  (let [trash-to-bottom? (<sub :epic/trash-to-bottom?)]
    [:<>
     [game-snackbar :epic/snackbar-1-open? :epic/snackbar-1-message]
     [game-snackbar :epic/snackbar-2-open? :epic/snackbar-2-message]
     (if trash-to-bottom?
       [trash-button])]))
