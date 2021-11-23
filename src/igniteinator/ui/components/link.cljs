(ns igniteinator.ui.components.link
  (:require [reagent-mui.material.link :refer [link]]))

(defn external-link [href & children]
  [link {:href   href
         :target "_blank"
         :rel    :noreferrer}
   children])
