(ns igniteinator.util.string)

(defn re-pattern-no-error [str]
  (try (re-pattern str)
       (catch js/SyntaxError _ nil)))
