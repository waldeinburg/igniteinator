(ns igniteinator.ui.base-filtering
  (:require [igniteinator.state :refer [assoc-a!]]
            [igniteinator.text :refer [txt]]
            [igniteinator.model.cards :refer [get-all-cards]]
            [reagent.core :as r]
            [reagent-material-ui.core.dialog :refer [dialog]]
            [reagent-material-ui.core.dialog-title :refer [dialog-title]]
            [reagent-material-ui.core.dialog-content :refer [dialog-content]]
            [reagent-material-ui.core.dialog-actions :refer [dialog-actions]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.form-control :refer [form-control]]
            [reagent-material-ui.core.form-label :refer [form-label]]
            [reagent-material-ui.core.form-group :refer [form-group]]
            [reagent-material-ui.core.form-control-label :refer [form-control-label]]
            [reagent-material-ui.core.checkbox :refer [checkbox]]
            [reagent-material-ui.lab.toggle-button-group :refer [toggle-button-group]]
            [reagent-material-ui.lab.toggle-button :refer [toggle-button]]))

(defn checkbox-elem [props]
  (r/as-element [checkbox props]))

(defn select-cards-dialog [card-selection-atom on-close]
  [dialog {:open (:dialog-open? @card-selection-atom) :on-close on-close}
   [dialog-title (txt :select-cards-dialog-title)]
   [dialog-content
    [form-control {:component "fieldset"}
     [form-group
      (doall
        (for [c (sort-by :name (get-all-cards))]
          (let [id (:id c)]
            ^{:key id}
            [form-control-label {:control (checkbox-elem
                                            {:checked   (contains? (:ids @card-selection-atom) id)
                                             :name      (str id)
                                             :on-change #(let [f (if (.. % -target -checked)
                                                                   conj disj)]
                                                           (swap! card-selection-atom update :ids f id))})
                                 :label   (:name c)}])))]]]])

(defn base-filtering [props]
  (let [{:keys [selected-value on-change on-dialog-close card-selection-atom]} props
        set-dialog-open! #(assoc-a! card-selection-atom :dialog-open? %)]
    [:<>
     [toggle-button-group {:value     selected-value, :exclusive true,
                           :on-change on-change
                           :size      :small}
      [toggle-button {:value :all} (txt :select-all)]
      [toggle-button {:value :some, :on-click #(set-dialog-open! true)} (txt :select-some)]]
     [select-cards-dialog
      card-selection-atom
      #(do (set-dialog-open! false)
           (on-dialog-close))]]))
