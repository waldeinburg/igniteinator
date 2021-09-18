(ns igniteinator.util.event)

(defn value [event]
  (.. event -target -value))

(defn checked? [event]
  (.. event -target -checked))
