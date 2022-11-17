(ns igniteinator.ui.components.bool-input
  (:require [igniteinator.util.event :as event]
            [reagent-mui.material.checkbox :refer [checkbox] :rename {checkbox mui-checkbox}]
            [reagent-mui.material.form-control-label :refer [form-control-label]]
            [reagent-mui.material.switch-component :refer [switch] :rename {switch mui-switch}]
            [reagent.core :as r]))

(defn- bool-input [elem {:keys [checked?-ref checked? label on-change wrapper-sx] :as props}]
  (let [cb [elem (into {:checked   (boolean
                                     (if (nil? checked?-ref)
                                       checked?
                                       @checked?-ref))
                        :name      label
                        :on-change #(on-change (event/checked? %))}
                   (dissoc props :label :checked?-ref :checked? :on-change :wrapper-sx))]]
    [form-control-label {:sx      wrapper-sx
                         :control (r/as-element cb)
                         :label   label}]))

(defn checkbox [props]
  (bool-input mui-checkbox props))

(defn switch [props]
  (bool-input mui-switch props))
