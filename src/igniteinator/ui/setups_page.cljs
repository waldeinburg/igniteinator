(ns igniteinator.ui.setups-page
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.page :refer [page]]
            [reagent-material-ui.core.list :refer [list]]
            [reagent-material-ui.core.list-item :refer [list-item]]
            [reagent-material-ui.core.list-item-text :refer [list-item-text]]))

(defn setups-list []
  (let [setups (<sub :setups)]
    [list {:component "nav"}
     (doall
       (for [s setups]
         ^{:key (:id s)}
         [list-item {:button   true
                     :on-click #(>evt :display-setup (:id s))}
          [list-item-text {:primary (:name s)}]]))]))

(defn setups-page []
  [page (txt :setups-title)
   [setups-list]])
