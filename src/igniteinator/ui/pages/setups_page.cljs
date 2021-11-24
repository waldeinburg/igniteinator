(ns igniteinator.ui.pages.setups-page
  (:require [igniteinator.text :refer [txt txt-c]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.util.event :as event]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [igniteinator.ui.components.checkbox :refer [checkbox]]
            [reagent.core :as r]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.list :refer [list]]
            [reagent-mui.material.list-item :refer [list-item]]
            [reagent-mui.material.list-item-text :refer [list-item-text]]
            [reagent-mui.material.divider :refer [divider]]
            [reagent-mui.material.form-group :refer [form-group]]
            [reagent-mui.material.select :refer [select]]
            [reagent-mui.material.menu-item :refer [menu-item]]))

(defn setups-filter-operator []
  [select {:variant   :standard
           :value     (<sub :setups-filter/operator)
           :on-change #(>evt :setups-filter/set-operator (event/value->keyword %))}
   [menu-item {:value :some} (txt-c :some-of)]
   [menu-item {:value :all} (txt-c :all-of)]])

(defn setups-box-selection-item [box]
  (let [id   (:id box)
        name (:name box)]
    [checkbox {:checked?-ref (<sub-ref :setups-filter/box-selected? id)
               :on-change    #(>evt :setups-filter/set-box-selected? id %)
               :disabled     (:required-by-all? box)
               :label        name}]))

(defn setups-filter-selection []
  (let [boxes (<sub :boxes-with-setups)]
    [box
     (doall
       (for [b boxes]
         ^{:key (:id b)}
         [setups-box-selection-item b]))]))

(defn setups-filtering []
  [form-item {:label (txt :required-boxes)}
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

     [list {:component "nav"
            :sx        {:width :fit-content}}
      (if includes-recommended-starter-set?
        [:<>
         (let [s (first setups)]
           ^{:key (:id s)} [setup-list-item s])
         [divider]])
      (doall
        (for [s main-list]
          ^{:key (:id s)}
          [setup-list-item s]))])])

(defn setups-page []
  [page (txt :setups-page-title)
   [setups-list]])
