(ns igniteinator.ui.hooks
  (:require [reagent-material-ui.core.use-media-query :refer [use-media-query]]))

(defn desktop-menu?-hook []
  (use-media-query "(min-width:725px)"))

(defn show-title-in-bar?-hook []
  (use-media-query "(min-width: 480px)"))
