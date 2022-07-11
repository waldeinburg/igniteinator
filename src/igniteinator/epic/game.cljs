(ns igniteinator.epic.game
  (:require [igniteinator.epic.reset-button :refer [reset-button]]
            [igniteinator.epic.trash :refer [trash-button]]
            [igniteinator.ui.components.bool-input :refer [switch]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.util.media-query :refer [min-max-media-query]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.icons.file-upload :refer [file-upload]]
            [reagent-mui.icons.low-priority :refer [low-priority]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.button-group :refer [button-group]]
            [reagent.core :as r]))

;; Presuppose default breakpoints: sm: 600px, md: 900px, lg: 1200px
(defn card-button-media-query [xs-min-width sm-min-width md-min-width lg-min-width]
  (min-max-media-query [(if xs-min-width [xs-min-width 599])
                        [sm-min-width 899]
                        [md-min-width 1199]
                        [lg-min-width]]))

(defn card-button [{:keys [color icon on-click]} label]
  [:f>
   (fn []
     (let [size               (<sub :size)
           s-labeled-buttons? (card-button-media-query nil 875 nil nil)
           m-labeled-buttons? (card-button-media-query 440 660 900 nil)
           l-labeled-buttons? (card-button-media-query 235 600 900 1200)]
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

(defn toggle-stack-info-switch []
  (switch {:checked?-ref (<sub-ref :epic/show-stack-info?)
           :on-change    #(>evt :epic/set-show-stack-info? %)
           :label        "Show stack info"}))

;; FIXME: emptying a stack lead to error
(defn stacks-display []
  (let [top-cards (<sub :epic/top-cards)]
    ;; TODO: Interaction
    ;; TODO: Show cards left. Or should that be in the popup to avoid cluttering the screen?
    ;; TODO: In Even More Epic Ignite, trashed cards go to bottom. Popup the search window.
    [card-list
     {:tooltip false                                        ;; Tooltip covers buttons
      :content-below-fn
      (fn [card]
        [:<>
         [box {:display :flex, :justify-content :center}
          [button-group
           [card-button {:color    :primary
                         :icon     [file-upload]
                         :on-click #(>evt :epic/take-card (:idx card))}
            "Take"]
           [card-button {:color :secondary
                         :icon  [low-priority]
                         :on-click #(>evt :epic/cycle-card (:idx card))}
            "Cycle"]]]
         (if (<sub :epic/show-stack-info?)
           [box {:mt 1}
            [:strong (:stack-name card)]
            " (" (:stack-count card) "): "
            [box {:component :span, :color "text.secondary"} (:stack-description card)]])])}
     top-cards]))

(defn epic-game []
  (if (<sub :epic/active?)
    [:<>
     [box {:mb 2}
      [reset-button]
      [trash-button]
      [toggle-stack-info-switch]]
     [stacks-display]]))