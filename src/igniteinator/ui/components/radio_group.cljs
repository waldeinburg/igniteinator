(ns igniteinator.ui.components.radio-group
  (:require [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.util.event :as event]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [reagent.core :as r]
            [reagent-material-ui.core.form-control-label :refer [form-control-label]]
            [reagent-material-ui.core.radio-group :refer [radio-group] :rename {radio-group mui-radio-group}]
            [reagent-material-ui.core.radio :refer [radio] :rename {radio mui-radio}]))

(defn radio-elem []
  (r/as-element [mui-radio]))

(defn radio [props]
  (let [radio-elem (radio-elem)]
    (fn [{:keys [value label on-click]
          :as   props}]
      [form-control-label (into
                            {:value    value
                             :control  radio-elem
                             :label    label
                             :on-click on-click}
                            (dissoc props :value :control :label :on-click))])))

(defn radio-group [{:keys [label value-ref value-type on-change]
                    :or   {value-type :keyword}
                    :as   props}
                   & children]
  (let [convert-value (case value-type
                        :number js/parseInt
                        :keyword keyword
                        :string identity)
        rg-props      (into
                        {:value     @value-ref
                         :on-change #(on-change (convert-value (event/value %)))}
                        (dissoc props :label :value-ref :value-type :on-change))]
    [form-item {:label label}
     [mui-radio-group rg-props
      (add-children children)]]))
