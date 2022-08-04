(ns igniteinator.ui.pages.card-details-page
  (:require [igniteinator.ui.components.card-details :refer [card-details-from-id]]
            [igniteinator.ui.components.page-with-navigation :refer [page-with-navigation]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]))

(defn card-details-page []
  (let [card-ids (<sub :card-details-page/card-ids)]
    [page-with-navigation
     {:idx-ref                  (<sub-ref :card-details-page/idx)
      :current-title-ref        (<sub-ref :card-details-page/current-card-name)
      :previous-title-ref       (<sub-ref :card-details-page/previous-card-name)
      :first-transition-in?-ref (<sub-ref :card-details-page/first-transition-in?)
      :on-change-index          #(>evt :card-details-page/set-idx %)}
     (for [id card-ids]
       ^{:key id}
       [card-details-from-id id])]))
