(ns igniteinator.util.event)

(defn target [event]
  (.-target event))

(defn value [event]
  (.-value (target event)))

(defn value->keyword [event]
  (keyword (value event)))

(defn checked? [event]
  (.-checked (target event)))

(defn prevent-default [event]
  (.preventDefault event))

(defn link-on-click [on-click]
  "Create event handler for links: Ignore on Ctrl-click (new tab) and prevent-default if handling."
  (fn [event]
    (when (not (.-ctrlKey event))
      (prevent-default event)
      (on-click event))))
