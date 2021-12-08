(ns igniteinator.ui.components.vendor.swipeable-views
  (:require [reagent.core :as r]
            ["react-swipeable-views" :as SwipeableViews]))

(def swipeable-views (r/adapt-react-class (.-default SwipeableViews)))
