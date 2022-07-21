(ns igniteinator.epic.page
  (:require [igniteinator.epic.game :refer [epic-game]]
            [igniteinator.epic.prepare :refer [prepare-game]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.re-frame :refer [<sub]]))

(defn epic-page []
  [page (txt :epic/page-title)
   (if (<sub :epic/active?)
     [epic-game]
     [prepare-game])])
