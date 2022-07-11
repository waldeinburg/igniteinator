(ns igniteinator.ui.components.bool-input
  (:require [igniteinator.util.event :as event]
            [reagent.core :as r]
            [reagent-mui.material.checkbox :refer [checkbox] :rename {checkbox mui-checkbox}]
            [reagent-mui.material.switch-component :refer [switch] :rename {switch mui-switch}]
            [reagent-mui.material.form-control-label :refer [form-control-label]]))

(defn- bool-input [elem {:keys [checked?-ref checked? label on-change] :as props}]
  (let [cb [elem (into {:checked   (boolean
                                             (if (nil? checked?-ref)
                                               checked?
                                               @checked?-ref))
                                :name      label
                                :on-change #(on-change (event/checked? %))}
                           (dissoc props :label :checked?-ref :checked? :on-change))]]
    [form-control-label {:control (r/as-element cb)
                         :label   label}]))

(defn checkbox [props]
  (bool-input mui-checkbox props))

(defn switch [props]
  (bool-input mui-switch props))
