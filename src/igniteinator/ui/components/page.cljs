(ns igniteinator.ui.components.page
  (:require [igniteinator.util.reagent :refer [add-children]]))

(defn page-title [title]
  [:h2 title])

(defn page [title & children]
  [:<>
   [page-title title]
   (add-children children)])
