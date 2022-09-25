(ns igniteinator.ui.pages.randomizer-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.page :refer [page]]))

(defn randomizer-page []
  (page (txt :randomizer/page-title)
    [:p "Not implemented yet – stay tuned!"]))
