(ns igniteinator.ui.components.card-list
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.constants :as constants]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.tooltip :refer [tooltip] :rename {tooltip tooltip-elem}]
            [igniteinator.ui.components.vendor.visibility-sensor :refer [visibility-sensor]]
            [reagent-mui.util :refer [adapt-react-class]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.grid :refer [grid]]))

(def placeholder-img-src (str constants/img-base-path "/placeholder.png"))

(defn card-debug-data [card]
  (if (<sub :debug/show-card-data)
    [:ul {:style {:list-style :none, :padding 0}}
     [:li [:strong "Box: "] (<sub :card/box-name card)]
     [:li [:strong "KS Exclusive: "] (if (<sub :card/ks-exclusive? card) "Yes" "No")]
     [:li [:strong "Types: "] (<sub :card/types-str card)]
     [:li [:strong "Cost: "] (str (:cost card))]]))

(defn card-name [card display-name?]
  (if (or display-name? (and (nil? display-name?) (<sub :display-name?)))
    [box {:align :center} (:name card)]))

(defn card-image
  ([card]
   [card-image {} card])
  ([props card]
   ;; Render a visibility-sensor until visible, then load image. It doesn't seem to hurt performance
   ;; to have a visibility sensor for each image. The alternative is having a visibility-sensor
   ;; after an increasing list of loaded cards, but the number of loaded cards should be reset when
   ;; the card list changes. The present solution is way more simple and also handles nicely
   ;; skipping images due to rapid scrolling.
   (fn [{:keys [on-click tooltip]} card]
     (let [src             (<sub :image-path card)
           load-state      (<sub :card-load-state card)
           name            (:name card)
           ;; The placeholder has the exact scale of the images.
           placeholder-img [:img {:src   placeholder-img-src, :alt name
                                  :class :card-img}]
           img             [:img (into {:src      src, :alt name
                                        :class    :card-img
                                        :on-click on-click
                                        :style    (if on-click {:cursor :pointer})}
                                   (if (not (= :loaded load-state))
                                     {:on-load #(>evt :set-card-load-state card :loaded)}))]]
       (cond
         ;; All ready. Just show image.
         (= :loaded load-state)
         (let []
           (if tooltip
             [tooltip-elem tooltip img]
             img))
         ;; The image is visible on screen but not loaded yet. Show the placeholder image still to
         ;; avoid the height of the container to be zero until the height of the image is loaded
         ;; which breaks scrolling to bottom (the height of the whole page will be correct
         ;; initially, then suddenly smaller, then back to the correct size). The image is loading
         ;; over the placeholder.
         (= :loading load-state)
         [:div {:class :dyn-height-block}
          [:div {:class :dyn-height-background} placeholder-img]
          [:div {:class :dyn-height-foreground} img]]
         ;; The image is not visible on screen. Avoid fetching until necessary.
         :else
         [visibility-sensor {:partial-visibility true
                             :on-change          #(when % (>evt :set-card-load-state card :loading))} ; %: visible?
          placeholder-img])))))

(defn card-container
  ([card]
   [card-container {} card])
  ([props card]
   [:<>
    [card-image props card]
    [card-name card (:display-name? props)]
    [card-debug-data card]]))

(defn card-grid [{:keys [grid-breakpoints-ref component]
                  :or {grid-breakpoints-ref (<sub-ref :grid-breakpoints)
                       component "li"}
                  :as props}
                 card]
  (let [grid-breakpoints @grid-breakpoints-ref
        grid-props (into {:component component, :item true} grid-breakpoints)]
    [grid grid-props
     [card-container props card]]))

(defn empty-card-list []
  [:p (str (txt-c :empty-list) ".")])

(defn card-list
  ([cards]
   (card-list {} cards))
  ([{on-click-prop    :on-click
     on-click-fn-prop :on-click-fn
     tooltip-prop     :tooltip
     tooltip-fn-prop  :tooltip-fn}
    cards]
   ;; Default tooltip and on-click is to show card details.
   (let [on-click-fn (cond
                       on-click-prop (fn [_] on-click-prop)
                       on-click-fn-prop on-click-fn-prop
                       :else (fn [card]
                               #(>evt :show-card-details card :page/push)))
         tooltip-fn  (cond
                       tooltip-prop (fn [_] tooltip-prop)
                       tooltip-fn-prop tooltip-fn-prop
                       :else (fn [card]
                               (if (not-empty (:combos card))
                                 (txt :card-tooltip-combos)
                                 (txt :card-tooltip-no-combos))))]
     (if (empty? cards)
       [empty-card-list]
       [grid {:component "ol", :container true, :spacing 1, :class "card-list"}
        (doall
          (for [c cards]
            (let [on-click (on-click-fn c)
                  tooltip  (tooltip-fn c)]
              ^{:key (:id c)}                               ; Cf. example on https://reagent-project.github.io/
              [card-grid {:on-click on-click, :tooltip tooltip} c])))]))))
