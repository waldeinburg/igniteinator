(ns igniteinator.ui.pages.randomizer-info-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.link :refer [internal-link]]
            [igniteinator.ui.components.page :refer [page]]))

(defn randomizer-info-page []
  (page (txt :randomizer/info-page-title)
    [back-button]
    [:p "TODO"]
    [:p "You can browse the " [internal-link :randomizer/data
                               {:navigate-event :page/to-other-sub-page}
                               "metadata"] "."]))
