(ns igniteinator.ui.share-button
  (:require [igniteinator.text :refer [txt txt-c]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.ui.dialog :refer [dialog]]
            [promesa.core :as p]
            [reagent.core :as r]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.form-group :refer [form-group]]
            [reagent-material-ui.core.text-field :refer [text-field]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.share :refer [share] :rename {share share-icon}]
            [reagent-material-ui.core.snackbar :refer [snackbar]]))

(defn url-text-field [url]
  (let [input-ref (r/atom nil)]
    [text-field {:input-ref #(reset! input-ref %)
                 :variant   :outlined
                 :margin    :dense
                 :value     url
                 :on-click  #(.select @input-ref)}]))

(defn url-copy-button [url]
  [button {:variant  :contained
           :on-click (fn [_]
                       (->
                         (.navigator.clipboard.writeText js/self url)
                         (p/then #(>evt :share/set-snackbar-open? true))
                         ;; Theoretically it could fail. Ignore for now.
                         (p/catch #(js/console.error %))))}
   (txt-c :copy)])

(defn share-dialog [url]
  [dialog {:title       (txt :share/dialog-title)
           :button-text (txt-c :close)
           :open?-ref   (<sub-ref :share/dialog-open?)
           :on-close    #(>evt :share/set-dialog-open? false)}
   [:p (txt :share/dialog-text)]
   [box {:display :flex, :align-items :center}
    [url-text-field url]
    [box {:ml 1}
     [url-copy-button url]]]])

(defn share-snackbar []
  [snackbar {:open               (<sub :share/snackbar-open?)
             :on-close           #(>evt :share/set-snackbar-open? false)
             :auto-hide-duration 4000
             :message            (txt :share/snackbar-text)}])

(defn share-button []
  (let [url (<sub :share/url)]
    [:<>
     [share-dialog url]
     [share-snackbar]
     (if url
       [icon-button {:on-click #(>evt :share/set-dialog-open? true)}
        [share-icon]])]))
