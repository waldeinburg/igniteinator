(ns igniteinator.ui.card-list
  (:require [igniteinator.state :refer [state language]]
            [igniteinator.constants :refer [img-base-path img-ext]]
            [igniteinator.constants :as constants]
            [reagent.core :as r]
            [reagent-material-ui.util :refer [adapt-react-class]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.grid :refer [grid]]
            [reagent-material-ui.core.tooltip :refer [tooltip]]
            ["react-visibility-sensor" :as VisibilitySensor]))

(def visibility-sensor (r/adapt-react-class (.-default VisibilitySensor)))

(defn card-image
  ([card]
   (card-image {} card))
  ([props card]
   (let [src     (str img-base-path "/" (name @language) "/" (:id card) img-ext)
         name    (:name card)
         ;; A little local state is ok here. Render a visibility-sensor until visible, then load
         ;; image. It doesn't seem to hurt performance to have a visibility sensor for each image.
         ;; The alternative is having a visibility-sensor after an increasing list of loaded cards,
         ;; but the number of loaded cards should be reset when the card list changes. The present
         ;; solution is way more simple and also handles nicely skipping images due to rapid
         ;; scrolling.
         loaded? (r/atom false)]
     (fn []
       (if @loaded?
         (let [img [:img {:src      src, :alt name
                          :class    ["card-img" (when (:on-click props) "MuiLink-button")]
                          :on-click (:on-click props)}]]
           (if-let [t (:tooltip props)]
             [tooltip {:title t, :arrow true} img]
             img))
         [visibility-sensor {:partial-visibility true
                             :on-change          #(when % (reset! loaded? true))} ; %: visible?
          [:div {:class "card-img card-not-loaded"}]])))))

(defn card-container
  ([card]
   (card-container {} card))
  ([props card]
   [card-image props card]))

(let [size (r/cursor state [:card-size])]
  (defn card-grid
    ([card]
     (card {} card))
    ([props card]
     [grid (into {:component "li", :item true} (get constants/card-sizes @size))
      [card-container props card]])))

(defn empty-card-list []
  [:p "Empty list."])

(defn card-list
  ([cards]
   (card-list {} cards))
  ([props cards]
   (let [on-click-fn (cond
                       (:on-click props) #(:on-click props)
                       (:on-click-fn props) (:on-click-fn props)
                       :else (constantly nil))
         tooltip-fn  (cond
                       (:tooltip props) #(:tooltip props)
                       (:tooltip-fn props) (:tooltip-fn props)
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
