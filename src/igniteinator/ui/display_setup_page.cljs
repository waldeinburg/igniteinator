(ns igniteinator.ui.display-setup-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.ui.back-button :refer [back-button]]
            [igniteinator.ui.card-list :refer [card-list]]))

(defn display-setup-page []
  (let [name      (<sub :current-setup/name)
        cards     (<sub :current-setup/cards)
        boxes-str (<sub :current-setup/required-boxes-string)]
    [page name
     [back-button]
     [:p (str (txt :required-boxes) ": " boxes-str)]
     [card-list cards]]))
