(ns igniteinator.ui.pages.display-setup-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.page-with-navigation :refer [page-with-navigation]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.icons.file-copy :refer [file-copy]]
            [reagent-mui.material.button :refer [button]]))

(defn copy-to-cards-page-button []
  [tooltip (txt :copy-to-cards-page-tooltip)
   [button {:variant  :outlined
            :color    :secondary
            :on-click #(>evt :current-setup/copy-to-cards-page)}
    [file-copy {:sx {:mr 0.5}}]
    (txt :copy-to-cards-page-button)]])

(defn- setup [id]
  (let [cards     (<sub :setup/cards id)
        boxes-str (<sub :setup/required-boxes-string id)]
    [:<>
     [:p (str (txt :required-boxes) ": " boxes-str)]
     [card-list cards]]))

(defn display-setup-page []
  (let [setup-ids (<sub :display-setup-page/setup-ids)]
    [page-with-navigation
     {:idx-ref                  (<sub-ref :display-setup-page/idx)
      :current-title-ref        (<sub-ref :display-setup-page/current-setup-name)
      :previous-title-ref       (<sub-ref :display-setup-page/previous-setup-name)
      :first-transition-in?-ref (<sub-ref :display-setup-page/first-transition-in?)
      :on-change-index          #(>evt :display-setup-page/set-idx %)
      :extra-buttons            [copy-to-cards-page-button]}
     (for [id setup-ids]
       ^{:key id}
       [setup id])]))
