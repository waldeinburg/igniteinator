(ns igniteinator.ui.pages.epic-pages
  (:require [igniteinator.epic.display-stack-page]
            [igniteinator.epic.page]
            [igniteinator.epic.trash]))

(defn display-stack-page []
  [igniteinator.epic.display-stack-page/display-stack-page])

(defn trash-page []
  [igniteinator.epic.trash/trash-page])

(defn epic-page []
  [igniteinator.epic.page/epic-page])
