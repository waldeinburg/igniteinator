(ns igniteinator.ui.cards-page
  (:require [igniteinator.state :refer [state]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.model.cards :refer [get-cards]]
            [reagent.core :as r]))

(let [page-state (r/cursor state [:cards-page])]
  (defn cards-page []
    [card-list (get-cards (:filters @page-state) (:sortings @page-state))]))
