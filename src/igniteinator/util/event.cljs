(ns igniteinator.util.event)

(defn target [event]
  (.-target event))

(defn value [event]
  (.-value (target event)))

(defn value->keyword [event]
  (keyword (value event)))

(defn checked? [event]
  (.-checked (target event)))
