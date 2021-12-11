(ns igniteinator.ui.components.page-with-navigation
  (:require [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.vendor.swipeable-views :refer [swipeable-views]]
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.modal :refer [modal]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.fade :refer [fade]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.icons.navigate-before :refer [navigate-before]]
            [reagent-mui.icons.navigate-next :refer [navigate-next]]
            [reagent-mui.icons.first-page :refer [first-page]]
            [reagent-mui.icons.last-page :refer [last-page]]))

(defn- content-view [{:keys [idx-ref on-change-index]} children]
  ;; Make the slides appear from the edge by canceling any padding using margin on the container and then adding the
  ;; same amount of padding on the children.
  ;; Margin cannot be applied to the slides containers so we cannot collapse margins. That would be nice because
  ;; the next slide would be right behind the edge.
  ;; Margins are the same as applied to the padding on the MUI Container.
  [box {:mx -2, :sm {:mx -3}}
   [swipeable-views {:animate-height  true                  ; The height of the slides may be very different.
                     :index           @idx-ref
                     :on-change-index on-change-index}
    (doall (map-indexed #(with-meta
                           [box {:px 2, :sm {:px 3}} %2]
                           {:key %1})
             children))]])

(defn- title [transition-in? t]
  [fade {:in      transition-in?
         :timeout 500
         :appear  false
         :sx      {:grid-row-start 1, :grid-column-start 1}}
   [box t]])

(defn nav-button [on-change-index new-idx enabled? icon]
  [button {:variant  :outlined
           :disabled (if (not enabled?) true)               ; nil if false
           :on-click (if enabled? #(on-change-index new-idx))}
   icon])

(defn- first-button [idx-ref on-change-index]
  (let [enabled? (> @idx-ref 0)]
    [nav-button on-change-index 0 enabled?
     [first-page]]))

(defn- prev-button [idx-ref on-change-index]
  (let [new-idx  (dec @idx-ref)
        enabled? (>= new-idx 0)]
    [nav-button on-change-index new-idx enabled?
     [navigate-before]]))

(defn- next-button [idx-ref on-change-index children-count]
  (let [new-idx  (inc @idx-ref)
        enabled? (< new-idx children-count)]
    [nav-button on-change-index new-idx enabled?
     [navigate-next]]))

(defn- last-button [idx-ref on-change-index children-count]
  (let [new-idx  (dec children-count)
        enabled? (< @idx-ref new-idx)]
    [nav-button on-change-index new-idx enabled?
     [last-page]]))

(defn- nav-bar [idx-ref on-change-index children-count extra-buttons]
  [toolbar {:disable-gutters true
            :sx              {:mb         1
                              :flex-wrap  :wrap
                              :column-gap 1
                              :row-gap    1}}
   [back-button {:variant :contained}]
   [box
    [first-button idx-ref on-change-index]
    [prev-button idx-ref on-change-index]
    [next-button idx-ref on-change-index children-count]
    [last-button idx-ref on-change-index children-count]]
   extra-buttons])

(defn page-with-navigation [{:keys [idx-ref
                                    current-title-ref
                                    previous-title-ref
                                    first-transition-in?-ref
                                    on-change-index
                                    extra-buttons]}
                            children]
  (let [current-title        @current-title-ref
        previous-title       @previous-title-ref
        first-transition-in? @first-transition-in?-ref]
    [:<>
     [page
      [box {:display :grid}
       [title
        first-transition-in?
        (if first-transition-in? current-title previous-title)]
       [title
        (not first-transition-in?)
        (if first-transition-in? previous-title current-title)]]
      [nav-bar idx-ref on-change-index (count children) extra-buttons]]
     [content-view {:idx-ref         idx-ref
                    :on-change-index on-change-index}
      children]]))
