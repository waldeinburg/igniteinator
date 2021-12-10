(ns igniteinator.ui.components.page-with-navigation
  (:require [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.vendor.swipeable-views :refer [swipeable-views]]
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.modal :refer [modal]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.fade :refer [fade]]))

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

(defn page-with-navigation [{:keys [idx-ref
                                    current-title-ref
                                    previous-title-ref
                                    first-transition-in?-ref
                                    on-change-index]}
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
        (if first-transition-in? previous-title current-title)]
       ]
      [box {:mb 2} [back-button]]]
     [content-view {:idx-ref         idx-ref
                    :on-change-index on-change-index}
      children]]))
