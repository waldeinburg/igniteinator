(ns igniteinator.util.event)

(defn target [event]
  (.-target event))

(defn value [event]
  (.-value (target event)))

(defn checked? [event]
  (.-checked (target event)))
