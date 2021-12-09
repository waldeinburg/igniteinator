(ns igniteinator.ui.pages.card-details
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.card-list :refer [card-list card-grid]]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.vendor.swipeable-views :refer [swipeable-views]]
            [reagent-mui.material.grid :refer [grid]]
            [reagent-mui.material.modal :refer [modal]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.fade :refer [fade]]))

(defn combos-list [card]
  (let [cards (<sub :card-details-page/combos card)]
    [card-list
     {:on-click-fn (fn [c]
                     #(>evt :show-card-details cards (:idx c) :page/replace))
      :tooltip-fn  (fn [c]
                     (case (:combos c)
                       [] (txt :card-tooltip-no-combos)
                       [(:id card)] (txt :card-tooltip-no-more-combos)
                       (txt :card-tooltip-combos)))}
     cards]))

(defn combos-section [card]
  [:<>
   [:h3 (txt :combos-title)]
   [combos-list card]])

(defn card-details [card-id]
  (let [card (<sub :card card-id)]
    ;; Show the next step larger than the card list.
    [:<>
     [grid {:container true}
      [card-grid {:component            "div"
                  :grid-breakpoints-ref (<sub-ref :grid-breakpoints+1)
                  :display-name?        false}
       card]]
     (if (empty? (:combos card))
       [:p (txt :no-combos)]
       [combos-section card])]))

(defn card-details-view []
  (let [card-ids (<sub :card-details-page/card-ids)
        idx      (<sub :card-details-page/initial-idx)]
    ;; Make the slides appear from the edge by canceling any padding using margin on the container and then adding the
    ;; same amount of padding on the children.
    ;; Margin cannot be applied to the slides containers so we cannot collapse margins. That would be nice because
    ;; the next slide would be right behind the edge.
    ;; Margins are the same as applied to the padding on the MUI Container.
    [box {:mx -2, :sm {:mx -3}}
     [swipeable-views {:animate-height  true                ; The height of the slides are very different.
                       :index           idx
                       :on-change-index #(>evt :card-details-page/set-idx %)}
      (for [id card-ids]
        ^{:key id}
        [box {:px 2, :sm {:px 3}}
         [card-details id]])]]))

(defn card-details-page []
  (let [card-titles (<sub :card-details-page/card-titles)]
    [:<>
     [page [:<>
            (txt :card-details-page-title) " "
            [box {:display :grid}
             (add-children
               (for [t card-titles]
                 [fade {:in      (:transition-in? t)
                        :timeout 500
                        :appear  false
                        :sx      {:grid-row-start 1, :grid-column-start 1}}
                  [box (:name t)]]))]]
      [box {:mb 2} [back-button]]]
     [card-details-view]]))
