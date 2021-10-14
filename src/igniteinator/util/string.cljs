(ns igniteinator.util.string)

(def format-re #"\\\{|\{([^}]+)\}")

(defn re-pattern-no-error [str]
  (try (re-pattern str)
       (catch js/SyntaxError _ nil)))

(defn format [str values]
  (clojure.string/replace str format-re
    (fn [m]
      (if (= "\\{" (get m 0))
        "{"
        (get values (keyword (get m 1)))))))
