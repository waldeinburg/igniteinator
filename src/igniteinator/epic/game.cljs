(ns igniteinator.epic.game
  (:require [igniteinator.epic.trash :refer [trash-button]]
            [igniteinator.epic.reset-button :refer [reset-button]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.media-query :refer [min-max-media-query]]
            [reagent.core :as r]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.button-group :refer [button-group]]
            [reagent-mui.icons.file-upload :refer [file-upload]]
            [reagent-mui.icons.low-priority :refer [low-priority]]))

;; Presuppose default breakpoints: sm: 600px, md: 900px, lg: 1200px
(defn card-button-media-query [xs-min-width sm-min-width md-min-width lg-min-width]
  (min-max-media-query [(if xs-min-width [xs-min-width 599])
                        [sm-min-width 899]
                        [md-min-width 1199]
                        [lg-min-width]]))



(defn card-button [{:keys [color icon on-click]} label]
  [:f>
   (fn []
     (let [size                (<sub :size)
           s-labeled-buttons?  (card-button-media-query nil 875 nil nil)
           m-labeled-buttons?  (card-button-media-query 440 660 900 nil)
           l-labeled-buttons?  (card-button-media-query 235 600 900 1200)]
       (if (or
             (and (= 0 size) s-labeled-buttons?)
             (and (= 1 size) m-labeled-buttons?)
             (and (<= 2 size) l-labeled-buttons?))
         [button {:variant    :contained
                  :color      color
                  :start-icon (r/as-element icon)
                  :on-click   on-click}
          label]
         [button {:variant  :contained
                  :color    color
                  :on-click on-click}
          icon])))])

(defn stacks-display []
  (let [top-cards (<sub :epic/top-cards)]
    ;; TODO: Interaction
    ;; TODO: Show questionmark icon which shows name and description in a popup.
    ;; TODO: Show cards left. Or should that be in the popup to avoid cluttering the screen?
    ;; TODO: In Even More Epic Ignite, trashed cards go to bottom. Popup the search window.
    [card-list
     {:tooltip false                                        ;; Tooltip covers buttons
      :content-below
      [box {:display :flex, :justify-content :center}
       [button-group
        [card-button {:color :primary
                      :icon  [file-upload]}
         "Take"]
        [card-button {:color :secondary
                      :icon  [low-priority]}
         "Cycle"]]]}
     top-cards]))

(defn epic-game []
  (if (<sub :epic/active?)
    [:<>
     [reset-button]
     [trash-button]
     [stacks-display]]))
