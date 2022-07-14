(ns igniteinator.epic.trash
  (:require [igniteinator.text :refer [txt-c]]
            [igniteinator.ui.components.back-button :refer [back-button]]
            [igniteinator.ui.components.card-list :refer [card-list]]
            [igniteinator.ui.components.dialog :refer [dialog]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [igniteinator.ui.components.page :refer [page]]
            [igniteinator.ui.components.search-bar :refer [search-bar]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [reagent-mui.icons.delete-forever :refer [delete-forever]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.form-group :refer [form-group]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent.core :as r]))

(defn trash-search-bar []
  [search-bar (<sub-ref :epic/trash-search-str) #(>evt :epic/set-trash-search-str %)])

(defn trash-page []
  (let [cards (<sub :epic/trash-page-cards)]
    [page "Select card to trash"
     [toolbar {:disable-gutters true}
      [back-button {:variant :contained
                    :sx      {:mr 2}}]
      [trash-search-bar]]
     [card-list
      {:tooltip     false
       :on-click-fn (fn [card]
                      #(>evt :epic/trash-card (:id card)))}
      cards]]))

(defn dialog-card-item [card]
  (let [id (:id card)]
    [button {:on-click #(>evt :epic/trash-card id)
             :variant  :outlined
             :sx       {:justify-content :left}}
     (:name card)]))

(defn select-cards-dialog []
  (let [cards (<sub :epic/trash-dialog-cards)]
    [dialog {:title        "Select card to trash"
             :button-text  (txt-c :cancel)
             :button-color :secondary
             :top?         true
             :open?-ref    (<sub-ref :epic/trash-dialog-open?)
             :on-close     #(>evt :epic/close-trash-menu)}
     [toolbar {:disable-gutters true}
      [trash-search-bar]]
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
                :on-click   #(>evt :epic/open-trash-menu)}
        "Trash"]])))
