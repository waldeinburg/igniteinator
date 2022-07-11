(ns igniteinator.ui.components.base-filtering
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.ui.components.search-bar :refer [search-bar]]
            [igniteinator.ui.components.dialog :refer [dialog]]
            [igniteinator.ui.components.radio-group :refer [radio-group radio]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [igniteinator.ui.components.bool-input :refer [checkbox]]
            [reagent.core :as r]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.button-group :refer [button-group]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.icons.check-box :refer [check-box] :rename {check-box check-box-icon}]
            [reagent-mui.icons.check-box-outline-blank :refer [check-box-outline-blank]]
            [reagent-mui.material.form-group :refer [form-group]]
            [reagent-mui.material.form-helper-text :refer [form-helper-text]]
            [reagent-mui.material.toggle-button-group :refer [toggle-button-group]]
            [reagent-mui.material.toggle-button :refer [toggle-button]]))

(defn combos-dialog [{:keys [open?-ref on-close value-ref on-change]}]
  [dialog {:title     (txt :combos-dialog-title)
           :open?-ref open?-ref
           :on-close  on-close}
   [radio-group {:value-ref value-ref
                 :on-change on-change}
    [radio {:value    :official
            :label    (r/as-element [:<> (txt :combos-dialog-official-item)
                                     [form-helper-text (txt :combos-dialog-official-help)]])
            :on-click on-close}]
    [radio {:value    :all
            :label    (txt :combos-dialog-all-item)
            :on-click on-close}]]])

(defn dialog-card-item [card <sub-dialog-item-selected?-ref on-dialog-item-selected-change]
  (let [id (:id card)]
    [checkbox {:checked?-ref (<sub-dialog-item-selected?-ref id)
               :name         (str id)
               :on-change    #(on-dialog-item-selected-change id %)
               :label        (:name card)}]))

(defn select-cards-dialog [{:keys [search-str-ref
                                   on-search-str-change
                                   open?-ref
                                   <sub-dialog-item-selected?-ref
                                   on-dialog-item-selected-change
                                   on-dialog-selection-set
                                   on-close]}]
  (let [cards (<sub :select-cards-dialog/cards)]
    [dialog {:title     (txt :select-cards-dialog-title)
             :top?      true
             :open?-ref open?-ref
             :on-close  on-close}
     [toolbar {:disable-gutters true}
      [button-group {:sx {:mr 1}}
       [tooltip (txt-c :select-all)
        [icon-button {:on-click #(on-dialog-selection-set :all)}
         [check-box-icon]]]
       [tooltip (txt-c :clear-selection)
        [icon-button {:on-click #(on-dialog-selection-set :none)}
         [check-box-outline-blank]]]]
      [search-bar search-str-ref on-search-str-change]]
     [form-item {}
      [form-group
       (doall
         (for [c cards]
           ^{:key (:id c)}
           [dialog-card-item c <sub-dialog-item-selected?-ref on-dialog-item-selected-change]))]]]))

(defn base-filtering [options]
  (fn [{:keys [select-dialog-open?-ref
               on-select-dialog-change-open
               combos-dialog-open?-ref
               on-combos-dialog-change-open
               combos-value-ref
               on-combos-change
               get-selected-value
               <sub-dialog-item-selected?-ref
               on-dialog-item-selected-change
               on-dialog-selection-set
               on-change]}]
    (let [search-str-ref       (<sub-ref :select-cards-dialog/search-str)
          on-search-str-change #(>evt :select-cards-dialog/set-search-str %)]
      [:<>
       [toggle-button-group {:value     (get-selected-value), :exclusive true,
                             :on-change #(on-change (keyword %2))
                             :size      :small}
        [toggle-button {:value :all} (txt-c :select-all-button)]
        [toggle-button {:value    :combos
                        :on-click #(on-combos-dialog-change-open true)}
         (txt-c :combos)]
        [toggle-button {:value    :some
                        :on-click (fn []
                                    ;; Reset search-str here to avoid resetting during the close animation.
                                    (on-search-str-change "")
                                    (on-select-dialog-change-open true))}
         (txt-c :select-some-button)]]
       [combos-dialog
        {:open?-ref combos-dialog-open?-ref
         :on-close  #(on-combos-dialog-change-open false)
         :value-ref combos-value-ref
         :on-change on-combos-change}]
       [select-cards-dialog
        {:search-str-ref                 search-str-ref
         :on-search-str-change           on-search-str-change
         :open?-ref                      select-dialog-open?-ref
         :<sub-dialog-item-selected?-ref <sub-dialog-item-selected?-ref
         :on-dialog-item-selected-change on-dialog-item-selected-change
         :on-dialog-selection-set        on-dialog-selection-set
         :on-close                       #(do (on-select-dialog-change-open false)
                                              (on-change :some))}]])))
