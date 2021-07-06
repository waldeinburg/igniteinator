(ns igniteinator.ui.page)

(defn page-title [title]
  [:h2 title])

(defn page [title & children]
  [:<>
   [page-title title]
   ;; https://github.com/reagent-project/reagent/issues/68#issuecomment-77635279
   (map-indexed #(with-meta %2 {:key %1}) children)])
