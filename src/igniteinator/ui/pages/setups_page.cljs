(ns igniteinator.ui.pages.setups-page
  (:require [igniteinator.text :refer [txt txt-c]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.event :as event]
            [igniteinator.ui.components.page :refer [page]]
            [reagent.core :as r]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.list :refer [list]]
            [reagent-material-ui.core.list-item :refer [list-item]]
            [reagent-material-ui.core.list-item-text :refer [list-item-text]]
            [reagent-material-ui.core.divider :refer [divider]]
            [reagent-material-ui.core.form-control :refer [form-control]]
            [reagent-material-ui.core.form-label :refer [form-label]]
            [reagent-material-ui.core.form-group :refer [form-group]]
            [reagent-material-ui.core.select :refer [select]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.core.checkbox :refer [checkbox]]
            [reagent-material-ui.core.form-control-label :refer [form-control-label]]))

(defn setups-filter-operator []
  [select {:value     (<sub :setups-filter/operator)
           :on-change #(>evt :setups-filter/set-operator (event/value->keyword %))}
   [menu-item {:value :some} (txt-c :some-of)]
   [menu-item {:value :all} (txt-c :all-of)]])

(defn setups-box-selection-item [box]
  (let [id   (:id box)
        name (:name box)
        cb   [checkbox {:checked   (<sub :setups-filter/box-selected? id)
                        :on-change #(>evt :setups-filter/set-box-selected? id (event/checked? %))
                        :name      name
                        :disabled  (:required-by-all? box)}]]
    [form-control-label {:control (r/as-element cb)
                         :label   name}]))

(defn setups-filter-selection []
  (let [boxes (<sub :boxes-with-setups)]
    [box
     (doall
       (for [b boxes]
         ^{:key (:id b)}
         [setups-box-selection-item b]))]))

(defn setups-filtering []
  [form-control {:component :fieldset}
   [form-label {:component :legend}
    (txt :required-boxes)]
   [form-group {:row true}
    [setups-filter-operator]
    [box {:ml 2}
     [setups-filter-selection]]]])

(defn setup-list-item [s]
  [list-item {:button   true
              :on-click #(>evt :display-setup (:id s))}
   [list-item-text {:primary (:name s)}]])

(defn setups-list []
  [:<>
   [setups-filtering]
   (let [setups                            (<sub :setups-filtered-and-sorted)
         includes-recommended-starter-set? (<sub :setups-includes-recommended-starter-set?)
         main-list                         (if includes-recommended-starter-set?
                                             (rest setups)
                                             setups)]
     [box {:width :fit-content}
      [list {:component "nav"}
       (if includes-recommended-starter-set?
         [:<>
          (let [s (first setups)]
            ^{:key (:id s)} [setup-list-item s])
          [divider]])
       (doall
         (for [s main-list]
           ^{:key (:id s)}
           [setup-list-item s]))]])])

(defn setups-page []
  [page (txt :setups-page-title)
   [setups-list]])