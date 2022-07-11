(ns igniteinator.epic.page
  (:require [igniteinator.epic.game :refer [epic-game]]
            [igniteinator.epic.prepare :refer [prepare-game]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.re-frame :refer [<sub >evt]]))

(defn epic-page []
  [page (txt :epic/page-title)
   [prepare-game]
   ;; TODO: Questionmark icon with help on how to use the interface.
   ;; TODO: Undo/redo and display last action.
   [epic-game]])
