(ns igniteinator.util.event)

(defn target [event]
  (. event -target))

(defn value [event]
  (. (target event) -value))

(defn checked? [event]
  (. (target event) -checked))
