(ns igniteinator.ui.base-filtering
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [igniteinator.text :refer [txt-c]]
            [igniteinator.util.event :refer [checked?]]
            [igniteinator.ui.tooltip :refer [tooltip]]
            [igniteinator.ui.search-bar :refer [search-bar]]
            [reagent.core :as r]
            [reagent-material-ui.styles :as styles]
            [reagent-material-ui.core.dialog :refer [dialog]]
            [reagent-material-ui.core.dialog-title :refer [dialog-title]]
            [reagent-material-ui.core.dialog-content :refer [dialog-content]]
            [reagent-material-ui.core.dialog-actions :refer [dialog-actions]]
            [reagent-material-ui.core.toolbar :refer [toolbar]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.button-group :refer [button-group]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.core.button :refer [button]]
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
  (let [filters  (if (empty? search-str)
                   []
                   [{:key :name-contains, :args [search-str]}])
        sortings [{:key :name, :order :asc}]]
    (<sub :cards :all filters sortings)))

;; Dialog will resize when filtering by name. Fix dialog at the top instead of center.
;; https://stackoverflow.com/a/61094451
(def dialog-at-top
  ((styles/with-styles {:scroll-paper {:align-items :baseline}}) dialog))

(defn dialog-card-item [card <sub-dialog-item-selected?-ref on-dialog-item-selected-change]
  (let [
        id (:id card)]
    [form-control-label {:control (checkbox-elem
                                    {:checked   @(<sub-dialog-item-selected?-ref id)
                                     :name      (str id)
                                     :on-change #(on-dialog-item-selected-change id (checked? %))})
                         :label   (:name card)}]))

(defn select-cards-dialog [search-str-atom
                           open?-ref
                           <sub-dialog-item-selected?-ref
                           on-dialog-item-selected-change
                           on-dialog-selection-set
                           on-close]
  (let [open? @open?-ref]
    [dialog-at-top {:open open?, :on-close on-close}
     [dialog-title (txt-c :select-cards-dialog-title)]
     [dialog-content
      [toolbar {:disable-gutters true}
       [box {:mr 1}
        [button-group
         [tooltip (txt-c :select-all)
          [icon-button {:on-click #(on-dialog-selection-set :all)}
           [check-box-icon]]]
         [tooltip (txt-c :clear-selection)
          [icon-button {:on-click #(on-dialog-selection-set :none)}
           [check-box-outline-blank]]]]]
       [search-bar search-str-atom #(reset! search-str-atom %)]]
      [form-control {:component "fieldset"}
       [form-group
        (doall
          (for [c (get-dialog-cards @search-str-atom)]
            ^{:key (:id c)}
            [dialog-card-item c <sub-dialog-item-selected?-ref on-dialog-item-selected-change]))]]]
     [dialog-actions
      [button {:on-click on-close} (txt-c :ok)]]]))

(defn base-filtering [options]
  (let [search-str-atom (r/atom "")]
    (fn [{:keys [dialog-open?-ref
                 on-dialog-change-open
                 selected-value
                 <sub-dialog-item-selected?-ref
                 on-dialog-item-selected-change
                 on-dialog-selection-set
                 on-change]}]
      [:<>
       [toggle-button-group {:value     selected-value, :exclusive true,
                             :on-change #(on-change (keyword %2))
                             :size      :small}
        [toggle-button {:value :all} (txt-c :select-all-button)]
        [toggle-button {:value :some, :on-click #(on-dialog-change-open true)} (txt-c :select-some-button)]]
       [select-cards-dialog
        search-str-atom
        dialog-open?-ref
        <sub-dialog-item-selected?-ref
        on-dialog-item-selected-change
        on-dialog-selection-set
        #(do (reset! search-str-atom "")
             (on-dialog-change-open false)
             (on-change :some))]])))
