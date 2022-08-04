(ns igniteinator.epic.display-stack-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.card-details :refer [card-details]]
            [igniteinator.ui.components.page-with-navigation :refer [page-with-navigation]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.icons.file-upload :refer [file-upload]]
            [reagent-mui.icons.low-priority :refer [low-priority]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.button-group :refer [button-group]]
            [reagent.core :as r]))

(defn stack-button [{:keys [tooltip-str disabled? color icon on-click]} label]
  (let [btn [button {:disabled   disabled?
                     :variant    :contained
                     :color      color
                     :start-icon (r/as-element icon)
                     :on-click   on-click}
             label]]
    (if disabled?
      btn
      [tooltip tooltip-str btn])))

(defn stack-buttons [card stack stack-count]
  [button-group
   {:disabled (or (:placeholder? stack) (= 0 stack-count))}
   [stack-button {:tooltip-str (txt :epic/take-button-tooltip {:name (:name card)})
                  :color       :primary
                  :icon        [file-upload]
                  :on-click    #(>evt :epic-display-stack-page/take-card (:idx stack))}
    (txt :epic/take-button-text)]
   [stack-button {:tooltip-str (txt :epic/cycle-button-tooltip {:name (:name card)})
                  :disabled?   (= 1 stack-count)
                  :color       :secondary
                  :icon        [low-priority]
                  :on-click    #(>evt :epic-display-stack-page/cycle-card (:idx stack))}
    (txt :epic/cycle-button-text)]])

(defn stack-details [top-card]
  (let [stack       (:stack top-card)
        stack-count (count (:cards stack))]
    [:<>
     [box {:mb 2} [stack-buttons top-card stack stack-count]]
     [box {:mb 2}
      [:strong (:name stack)]
      (if (not (:placeholder? stack))
        (str " (" stack-count ")"))
      ": "
      [box {:component :span, :color "text.secondary"} (:description stack)]]
     [card-details top-card]]))

(defn display-stack-page []
  (let [cards (<sub :epic/relevant-top-cards)]
    [page-with-navigation
     {:idx-ref                  (<sub-ref :epic-display-stack-page/idx)
      :current-title-ref        (<sub-ref :epic-display-stack-page/current-title)
      :previous-title-ref       (<sub-ref :epic-display-stack-page/previous-title)
      :first-transition-in?-ref (<sub-ref :epic-display-stack-page/first-transition-in?)
      :on-change-index          #(>evt :epic-display-stack-page/set-idx %)}
     (for [c cards]
       ^{:key (-> c :stack :idx)}
       [stack-details c])]))
