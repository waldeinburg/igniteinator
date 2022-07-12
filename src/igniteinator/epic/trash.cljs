(ns igniteinator.epic.trash
  (:require [igniteinator.text :refer [txt-c]]
            [igniteinator.ui.components.dialog :refer [dialog]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [igniteinator.ui.components.search-bar :refer [search-bar]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.icons.delete-forever :refer [delete-forever]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.form-group :refer [form-group]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent.core :as r]))

(defn dialog-card-item [card]
  (let [id (:id card)]
    [button {:on-click #(>evt :epic/trash-card id)
             :variant  :outlined
             :sx       {:justify-content :left}}
     (:name card)]))

(defn select-cards-dialog []
  (let [cards (<sub :epic/trash-dialog-cards)]
    [dialog {:title        "Select card"
             :button-text  (txt-c :cancel)
             :button-color :secondary
             :top?         true
             :open?-ref    (<sub-ref :epic/trash-dialog-open?)
             :on-close     #(>evt :epic/set-trash-dialog-open? false)}
     [toolbar {:disable-gutters true}
      [search-bar (<sub-ref :epic/trash-search-str) #(>evt :epic/set-trash-search-str %)]]
     [form-item {}
      [form-group
       (doall
         (for [c cards]
           ^{:key (:id c)}
           [dialog-card-item c]))]]]))

(defn trash-button []
  (let [setup     (<sub :epic/setup)
        disabled? (<sub :epic/trash-button-disabled?)]
    (if (:trash-to-bottom? setup)
      [:<>
       [select-cards-dialog]
       [button {:variant    :outlined
                :sx         {:mr 2}
                :disabled   disabled?
                :start-icon (r/as-element [delete-forever])
                :on-click   #(>evt :epic/set-trash-dialog-open? true)}
        "Trash"]])))
