(ns igniteinator.ui.base-filtering
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.model.cards :refer [get-all-cards]]
            [reagent.core :as r]
            [reagent-material-ui.core.form-control :refer [form-control]]
            [reagent-material-ui.core.form-label :refer [form-label]]
            [reagent-material-ui.core.form-group :refer [form-group]]
            [reagent-material-ui.core.form-control-label :refer [form-control-label]]
            [reagent-material-ui.core.checkbox :refer [checkbox]]
            [reagent-material-ui.lab.toggle-button-group :refer [toggle-button-group]]
            [reagent-material-ui.lab.toggle-button :refer [toggle-button]]))

(defn checkbox-elem [props]
  (r/as-element [checkbox props]))

(defn select-cards-dialog [card-selection-cursor]
  [form-control {:component "fieldset"}
   [form-label {:component "legend"} "foo"]
   [form-group
    (doall
      (for [c (sort-by :name (get-all-cards))]
        (let [id (:id c)]
          ^{:key id}
          [form-control-label {:control (checkbox-elem
                                          {:checked   (contains? @card-selection-cursor id)
                                           :name      (str id)
                                           :on-change #(let [f (if (.. % -target -checked)
                                                                 conj disj)]
                                                         (swap! card-selection-cursor f id))})
                               :label   (:name c)}])))]])

(defn base-filtering [props]
  (let [{:keys [selected-value on-change card-selection-cursor]} props]
    [:<>
     [select-cards-dialog card-selection-cursor]
     [toggle-button-group {:value selected-value, :exclusive true, :on-change on-change
                           :size  :small}
      [toggle-button {:value :all} (txt :select-all)]
      [toggle-button {:value :some} (txt :select-some)]]]))
