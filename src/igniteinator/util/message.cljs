(ns igniteinator.util.message)

(defn message-handler [handler-fn]
  (fn [e]
    (let [msg      (js->clj (.-data e) :keywordize-keys true)
          msg-type (keyword (:type msg))
          msg-data (:data msg)]
      (handler-fn msg-type msg-data e))))

(defn post
  ([receiver msg-type]
   (post receiver msg-type nil))
  ([receiver msg-type msg-data]
   (.postMessage receiver (clj->js {:type msg-type, :data msg-data}))))
