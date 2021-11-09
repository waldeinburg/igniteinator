(ns igniteinator.ui.singletons.share-button
  (:require [igniteinator.text :refer [txt txt-c]]
            [igniteinator.util.event :as event]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.ui.components.dialog :refer [dialog]]
            [promesa.core :as p]
            [reagent.core :as r]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.form-group :refer [form-group]]
            [reagent-material-ui.core.text-field :refer [text-field]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.select :refer [select]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.share :refer [share] :rename {share share-icon}]
            [reagent-material-ui.core.snackbar :refer [snackbar]]))

(defn share-mode-select []
  [select {:value     (<sub :share/mode)
           :on-change #(>evt :share/set-mode (event/value->keyword %))}
   [menu-item {:value :url} (txt-c :link)]
   [menu-item {:value :names} (txt-c :names)]])

(defn share-text-field [url]
  (let [input-ref (r/atom nil)]
    [text-field {:input-ref #(reset! input-ref %)
                 :variant   :outlined
                 :margin    :dense
                 :value     url
                 :on-click  #(.select @input-ref)}]))

(defn copy-button [value]
  [button {:variant  :contained
           :on-click (fn [_]
                       (->
                         (.navigator.clipboard.writeText js/self value)
                         (p/then #(>evt :share/set-snackbar-open? true))
                         ;; Theoretically it could fail. Ignore for now.
                         (p/catch #(js/console.error %))))}
   (txt-c :copy)])

(defn share-dialog [value]
  [dialog {:title       (txt :share/dialog-title)
           :button-text (txt-c :close)
           :open?-ref   (<sub-ref :share/dialog-open?)
           :on-close    #(>evt :share/set-dialog-open? false)}
   [:p (txt :share/dialog-text)]
   [form-group
    [box {:display :flex, :align-items :center}
     [share-text-field value]
     [box {:ml 1}
      [copy-button value]]]
    [box
     [share-mode-select]]]])

(defn share-snackbar []
  [snackbar {:open               (<sub :share/snackbar-open?)
             :on-close           #(>evt :share/set-snackbar-open? false)
             :auto-hide-duration 4000
             :message            (<sub :share/snackbar-text-formatted)}])

(defn share-button []
  (let [value (<sub :share/value)]
    [:<>
     [share-dialog value]
     [share-snackbar]
     (if value
       [icon-button {:on-click #(>evt :share/set-dialog-open? true)}
        [share-icon]])]))
