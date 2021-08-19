(ns igniteinator.util.event)

(defn value [event]
  (.. event -target -value))
