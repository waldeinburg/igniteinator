(ns igniteinator.ui.setups-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.page :refer [page]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.list :refer [list]]
            [reagent-material-ui.core.list-item :refer [list-item]]
            [reagent-material-ui.core.list-item-text :refer [list-item-text]]
            [reagent-material-ui.core.divider :refer [divider]]))

(defn setup-list-item [s]
  [list-item {:button   true
              :on-click #(>evt :display-setup (:id s))}
   [list-item-text {:primary (:name s)}]])

(defn setups-list []
  (let [setups (<sub :setups-sorted)]
    [box {:width :fit-content}
     [list {:component "nav"}
      (let [s (first setups)]
        ^{:key (:id s)} [setup-list-item s])
      [divider]
      (doall
        (for [s (rest setups)]
          ^{:key (:id s)}
          [setup-list-item s]))]]))

(defn setups-page []
  [page (txt :setups-page-title)
   [setups-list]])
