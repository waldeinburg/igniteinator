(ns igniteinator.ui.search-bar
  (:require [igniteinator.util.event :as event]
            [reagent.core :as r]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.text-field :refer [text-field]]
            [reagent-material-ui.core.input :refer [input]]
            [reagent-material-ui.core.input-adornment :refer [input-adornment]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.search :refer [search] :rename {search search-icon}]
            [reagent-material-ui.icons.clear :refer [clear] :rename {clear clear-icon}]))

(defn search-and-clear-icon [input-ref search-str-atom]
  (r/as-element
    [input-adornment {:position :end}
     (if (empty? @search-str-atom)
       [icon-button {:disabled true} [search-icon]]
       [icon-button {:on-click #(do (set! (.-value @input-ref))
                                    (reset! search-str-atom ""))}
        [clear-icon]])]))

(defn search-bar [_ _]
  (let [input-ref (r/atom nil)]
    (fn [search-str-atom {:keys [placeholder on-change] :or {on-change #()}}]
      [input {:input-ref     #(reset! input-ref %)
              :default-value @search-str-atom               ; not :value (will cause rerendering on input)
              :placeholder   placeholder
              :on-change     (fn [ev]
                               (let [v (event/value ev)]
                                 (reset! search-str-atom v)
                                 (on-change v)))
              :end-adornment (search-and-clear-icon input-ref search-str-atom)}])))
