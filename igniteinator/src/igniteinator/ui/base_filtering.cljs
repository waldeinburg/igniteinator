(ns igniteinator.ui.base-filtering
  (:require [igniteinator.state :refer [assoc-a!]]
            [igniteinator.text :refer [txt-c]]
            [igniteinator.model.cards :refer [get-all-card-ids get-cards]]
            [igniteinator.ui.tooltip :refer [tooltip]]
            [igniteinator.ui.search-bar :refer [search-bar]]
            [reagent.core :as r]
            [reagent-material-ui.styles :as styles]
            [reagent-material-ui.core.dialog :refer [dialog]]
            [reagent-material-ui.core.dialog-title :refer [dialog-title]]
            [reagent-material-ui.core.dialog-content :refer [dialog-content]]
            [reagent-material-ui.core.dialog-actions :refer [dialog-actions]]
            [reagent-material-ui.core.toolbar :refer [toolbar]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.check-box :refer [check-box] :rename {check-box check-box-icon}]
            [reagent-material-ui.icons.check-box-outline-blank :refer [check-box-outline-blank]]
            [reagent-material-ui.core.form-control :refer [form-control]]
            [reagent-material-ui.core.form-label :refer [form-label]]
            [reagent-material-ui.core.form-group :refer [form-group]]
            [reagent-material-ui.core.form-control-label :refer [form-control-label]]
            [reagent-material-ui.core.checkbox :refer [checkbox]]
            [reagent-material-ui.lab.toggle-button-group :refer [toggle-button-group]]
            [reagent-material-ui.lab.toggle-button :refer [toggle-button]]))

(defn checkbox-elem [props]
  (r/as-element [checkbox props]))

(defn get-dialog-cards [search-str]
  (get-cards :all
    [{:key :name-contains, :args [search-str]}]
    [{:key :name, :order :asc}]))

;; Dialog will resize when filtering by name. Fix dialog at the top instead of center.
;; https://stackoverflow.com/a/61094451
(def dialog-at-top
  ((styles/with-styles {:scroll-paper {:align-items :baseline}}) dialog))

(defn select-cards-dialog [card-selection-atom on-close]
  (let [search-str (r/cursor card-selection-atom [:search-str])]
    [dialog-at-top {:open (:dialog-open? @card-selection-atom) :on-close on-close}
     [dialog-title (txt-c :select-cards-dialog-title)]
     [dialog-content
      [toolbar {:disable-gutters true}
       [tooltip (txt-c :select-all)
        [icon-button {:on-click #(assoc-a! card-selection-atom
                                   :ids (set (get-all-card-ids)))}
         [check-box-icon]]]
       [tooltip (txt-c :clear-selection)
        [icon-button {:on-click #(assoc-a! card-selection-atom :ids #{})}
         [check-box-outline-blank]]]
       [search-bar
        search-str
        {:placeholder (str (txt-c :search) " …")}]]
      [form-control {:component "fieldset"}
       [form-group
        (doall
          (for [c (get-dialog-cards @search-str)]
            (let [id (:id c)]
              ^{:key id}
              [form-control-label {:control (checkbox-elem
                                              {:checked   (contains? (:ids @card-selection-atom) id)
                                               :name      (str id)
                                               :on-change #(let [f (if (.. % -target -checked)
                                                                     conj disj)]
                                                             (swap! card-selection-atom update :ids f id))})
                                   :label   (:name c)}])))]]]]))

(defn base-filtering [{:keys [selected-value on-change card-selection-atom]}]
  (let [set-dialog-open! #(assoc-a! card-selection-atom :dialog-open? %)]
    [:<>
     [toggle-button-group {:value     selected-value, :exclusive true,
                           :on-change #(on-change (keyword %2))
                           :size      :small}
      [toggle-button {:value :all} (txt-c :select-all-button)]
      [toggle-button {:value :some, :on-click #(set-dialog-open! true)} (txt-c :select-some-button)]]
     [select-cards-dialog
      card-selection-atom
      #(do (set-dialog-open! false)
           (on-change :some))]]))
