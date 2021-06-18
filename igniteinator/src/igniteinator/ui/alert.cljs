(ns igniteinator.ui.alert
  (:require [igniteinator.state :refer [set-state!]]))

(defn fatal! [message]
  (set-state!
    :mode :fatal-error
    :fatal-message message))
