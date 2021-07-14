(ns igniteinator.ui.cards-page
  (:require [igniteinator.state :refer [state set-in-state! assoc-a! assoc-in-a!]]
            [igniteinator.ui.base-filtering :refer [base-filtering]]
            [igniteinator.ui.card-list :refer [card-list]]
            [igniteinator.ui.page :refer [page]]
            [igniteinator.ui.search-bar :refer [search-bar]]
            [igniteinator.model.cards :refer [get-cards]]
            [igniteinator.text :refer [txt-c]]
            [reagent.core :as r]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.toolbar :refer [toolbar]]))

(defonce current-page (r/cursor state [:current-page]))
(defonce page-state (r/cursor state [:cards-page]))
(defonce card-selection-cursor (r/cursor page-state [:card-selection]))
(defonce search-str (r/cursor page-state [:search-str]))

(defn show-combos [card]
  (set-in-state!
    [:current-page] :combos
    [:previous-page] :cards
    [:combos-page :card-id] (:id card)))

(defn cards-page []
  (when (= :cards @current-page)
    [page (txt-c :cards-page-title)
     [toolbar {:disable-gutters true}
      [box {:mr 2}
       [base-filtering
        {:selected-value      (if (= :all (:base @page-state))
                                :all
                                :some)
         :on-change           #(case %
                                 :all (assoc-a! page-state :base :all)
                                 :some (assoc-a! page-state :base (:ids @card-selection-cursor))
                                 ;; When a button is clicked without being changed.
                                 nil nil)
         :card-selection-atom card-selection-cursor}]]
      [search-bar search-str]]
     [card-list
      {:on-click-fn (fn [card]
                      (when (not-empty (:combos card))
                        #(show-combos card)))
       :tooltip-fn  (fn [card]
                      (if (not-empty (:combos card))
                        (txt-c :show-combos)
                        (txt-c :no-combos)))}
      (let [base-filters (:filters @page-state)
            s-str        @search-str
            filters      (if (empty? s-str)
                           base-filters
                           (conj base-filters {:key :name-contains, :args [s-str]}))]
        (get-cards (:base @page-state) filters (:sortings @page-state)))]]))
