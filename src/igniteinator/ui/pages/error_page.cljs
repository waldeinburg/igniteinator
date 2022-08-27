(ns igniteinator.ui.pages.error-page
  (:require [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.util.image-path :refer [image-path]]))

(defn error-page []
  (page "Error"
    [card-list
     {:on-click false
      :tooltip  false}
     (mapv (fn [lang]
             {:id         lang
              :image-path (image-path lang {:id "hex"})})
       [:en :de :es :fr])]))
