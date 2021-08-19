(ns igniteinator.ui.link
  (:require [reagent-material-ui.core.link :refer [link]]))

(defn external-link [href & children]
  [link {:href   href
         :target "_blank"
         :rel    :noreferrer}
   children])
