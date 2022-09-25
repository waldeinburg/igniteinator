(ns igniteinator.util.reagent)

(defn add-children [children]
  ;; https://github.com/reagent-project/reagent/issues/68#issuecomment-77635279
  (doall (map-indexed #(with-meta %2 {:key %1}) children)))

(defn destruct-props-children [items]
  (if (map? (first items))
    [(first items) (rest items)]
    [{} items]))
