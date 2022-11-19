(ns igniteinator.ui.hooks
  (:require [reagent-mui.material.use-media-query :refer [use-media-query]]))

(defn desktop-menu?-hook []
  (use-media-query "(min-width:860px)"))

(defn show-title-in-bar?-hook []
  (use-media-query "(min-width: 480px)"))
