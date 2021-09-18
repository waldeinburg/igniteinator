(ns igniteinator.ui.card-list
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [igniteinator.constants :as constants]
            [igniteinator.text :refer [txt-c]]
            [igniteinator.util.image-path :refer [image-path]]
            [igniteinator.ui.tooltip :refer [tooltip] :rename {tooltip mui-tooltip}]
            [reagent.core :as r]
            [reagent-material-ui.util :refer [adapt-react-class]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.grid :refer [grid]]
            ["react-visibility-sensor" :as VisibilitySensor]))

(def placeholder-img-src (str constants/img-base-path "/placeholder.png"))
(def visibility-sensor (r/adapt-react-class (.-default VisibilitySensor)))

(defn card-image
  ([card]
   [card-image {} card])
  ([props card]
   (let [;; A little local state is ok here. Render a visibility-sensor until visible, then load
         ;; image. It doesn't seem to hurt performance to have a visibility sensor for each image.
         ;; The alternative is having a visibility-sensor after an increasing list of loaded cards,
         ;; but the number of loaded cards should be reset when the card list changes. The present
         ;; solution is way more simple and also handles nicely skipping images due to rapid
         ;; scrolling.
         loading? (r/atom false)
         loaded?  (r/atom false)]
     (fn [{:keys [on-click tooltip]} card]
       (let [lang            (<sub :language)
             src             (image-path lang card)
             name            (:name card)
             ;; The placeholder has the exact scale of the images.
             placeholder-img [:img {:src   placeholder-img-src, :alt name
                                    :class "card-img"}]
             img             [:img {:on-load  #(reset! loaded? true)
                                    :src      src, :alt name
                                    :class    ["card-img" (when on-click "MuiLink-button")]
                                    :on-click on-click}]]
         (cond
           ;; All ready. Just show image.
           @loaded?
           (let []
             (if tooltip
               [mui-tooltip tooltip img]
               img))
           ;; The image is visible on screen but not loaded yet. Show the placeholder image still to
           ;; avoid the height of the container to be zero until the height of the image is loaded
           ;; which breaks scrolling to bottom (the height of the whole page will be correct
           ;; initially, then suddenly smaller, then back to the correct size). The image is loading
           ;; over the placeholder.
           @loading?
           [:div {:class :dyn-height-block}
            [:div {:class :dyn-height-background} placeholder-img]
            [:div {:class :dyn-height-foreground} img]]
           ;; The image is not visible on screen. Avoid fetching until necessary.
           :else
           [visibility-sensor {:partial-visibility true
                               :on-change          #(when % (reset! loading? true))} ; %: visible?
            placeholder-img]))))))

(defn card-container
  ([card]
   [card-container {} card])
  ([props card]
   [card-image props card]))

(defn card-grid
  ([card]
   (card {} card))
  ([props card]
   (let [size (<sub :card-size)]
     [grid (into {:component "li", :item true} (get constants/card-sizes size))
      [card-container props card]])))

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
   (let [on-click-fn (cond
                       on-click-prop (fn [_] on-click-prop)
                       on-click-fn-prop on-click-fn-prop
                       :else (constantly nil))
         tooltip-fn  (cond
                       tooltip-prop (fn [_] tooltip-prop)
                       tooltip-fn-prop tooltip-fn-prop
                       :else (constantly nil))]
     (if (empty? cards)
       [empty-card-list]
       [grid {:component "ol", :container true, :class "card-list"}
        (doall
          (for [c cards]
            (let [on-click (on-click-fn c)
                  tooltip  (tooltip-fn c)]
              ^{:key (:id c)}                               ; Cf. example on https://reagent-project.github.io/
              [card-grid {:on-click on-click, :tooltip tooltip} c])))]))))
