(ns igniteinator.ui.components.vendor.visibility-sensor
  (:require [reagent.core :as r]
            ["react-visibility-sensor" :as VisibilitySensor]))

(def visibility-sensor (r/adapt-react-class (.-default VisibilitySensor)))
