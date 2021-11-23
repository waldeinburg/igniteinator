(ns igniteinator.ui.components.checkbox
  (:require [igniteinator.util.event :as event]
            [reagent.core :as r]
            [reagent-mui.material.checkbox :refer [checkbox] :rename {checkbox mui-checkbox}]
            [reagent-mui.material.form-control-label :refer [form-control-label]]))

(defn checkbox [{:keys [checked?-ref checked? label on-change] :as props}]
  (let [cb [mui-checkbox (into {:checked   (boolean
                                             (if (nil? checked?-ref)
                                               checked?
                                               @checked?-ref))
                                :name      label
                                :on-change #(on-change (event/checked? %))}
                           (dissoc props :label :checked?-ref :checked? :on-change))]]
    [form-control-label {:control (r/as-element cb)
                         :label   label}]))
