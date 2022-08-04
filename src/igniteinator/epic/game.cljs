(ns igniteinator.epic.game
  (:require [clojure.string :as s]
            [igniteinator.epic.game-toolbar :refer [game-toolbar]]
            [igniteinator.epic.reset-button :refer [reset-button]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.bool-input :refer [switch]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.util.media-query :refer [min-max-media-query]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.icons.file-upload :refer [file-upload]]
            [reagent-mui.icons.low-priority :refer [low-priority]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.button-group :refer [button-group]]
            [reagent.core :as r]))

(defn debug-copy-stack-cards [card-ids]
  (.navigator.clipboard.writeText js/self
    (str "https://localhost:9533/?ids=" (s/join "," (sort card-ids)))))

;; Presuppose default breakpoints: sm: 600px, md: 900px, lg: 1200px
(defn card-button-media-query [xs-min-width sm-min-width md-min-width lg-min-width]
  (min-max-media-query [(if xs-min-width [xs-min-width 599])
                        (if sm-min-width [sm-min-width 899])
                        (if md-min-width [md-min-width 1199])
                        (if lg-min-width [lg-min-width])]))

(defn card-button [{:keys [tooltip-str disabled? color icon on-click]} label]
  [:f>
   (fn []
     (let [size               (<sub :size)
           s-labeled-buttons? (card-button-media-query nil 875 nil nil)
           m-labeled-buttons? (card-button-media-query 440 660 900 nil)
           l-labeled-buttons? (card-button-media-query 235 600 900 1200)
           btn                (if (or
                                    (and (= 0 size) s-labeled-buttons?)
                                    (and (= 1 size) m-labeled-buttons?)
                                    (and (<= 2 size) l-labeled-buttons?))
                                [button {:disabled   disabled?
                                         :variant    :contained
                                         :color      color
                                         :start-icon (r/as-element icon)
                                         :on-click   on-click}
                                 label]
                                [button {:disabled disabled?
                                         :variant  :contained
                                         :color    color
                                         :on-click on-click}
                                 icon])]
       (if disabled?
         btn
         [tooltip tooltip-str btn])))])

(defn card-content-below [card]
  (let [stack       (:stack card)
        stack-count (count (:cards stack))]
    ;; The :placeholder? property allows us to merge ind dummy decks with March, Dagger, Old Wooden Shield and
    ;; even Hex. This should be an opt-in, though.
    [:<>
     [box {:display :flex, :justify-content :center}
      [button-group
       {:disabled (or (:placeholder? stack) (= 0 stack-count))}
       [card-button {:tooltip-str (txt :epic/take-button-tooltip {:name (:name card)})
                     :color       :primary
                     :icon        [file-upload]
                     :on-click    #(>evt :epic/take-card (:idx stack))}
        (txt :epic/take-button-text)]
       [card-button {:tooltip-str (txt :epic/cycle-button-tooltip {:name (:name card)})
                     :disabled?   (= 1 stack-count)
                     :color       :secondary
                     :icon        [low-priority]
                     :on-click    #(>evt :epic/cycle-card (:idx stack))}
        (txt :epic/cycle-button-text)]]]
     (if (<sub :epic/show-stack-info?)
       [box {:mt 1}
        [:strong (:name stack)]
        (if (not (:placeholder? stack))
          (str " (" stack-count ")"))
        ": "
        [box {:component :span, :color "text.secondary"} (:description stack)]])]))

(defn toggle-stack-info-switch []
  (switch {:checked?-ref (<sub-ref :epic/show-stack-info?)
           :on-change    #(>evt :epic/set-show-stack-info? %)
           :label        "Show stack info"}))

(defn stacks-display []
  (let [top-cards (<sub :epic/top-cards)]
    [card-list
     {:tooltip          false                               ;; Tooltip covers buttons
      :on-click-fn      (fn [card]
                          (let [stack (:stack card)]
                            (if (or (:placeholder? stack) (= 0 (count (:cards stack))))
                              nil
                              #(>evt :epic/show-stack (:nav-stack-idx card)))))
      :content-below-fn card-content-below}
     top-cards]))

(defn epic-game []
  [:<>
   [box {:mb 2}
    [reset-button]
    [toggle-stack-info-switch]]
   [stacks-display]
   [game-toolbar]])
