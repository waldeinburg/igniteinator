(ns igniteinator.ui.components.base-filtering
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.util.event :as event]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [igniteinator.ui.components.search-bar :refer [search-bar]]
            [igniteinator.ui.components.dialog :refer [dialog]]
            [reagent.core :as r]
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
            [reagent-material-ui.core.radio-group :refer [radio-group]]
            [reagent-material-ui.core.radio :refer [radio] :rename {radio mui-radio}]
            [reagent-material-ui.core.form-helper-text :refer [form-helper-text]]
            [reagent-material-ui.core.checkbox :refer [checkbox]]
            [reagent-material-ui.lab.toggle-button-group :refer [toggle-button-group]]
            [reagent-material-ui.lab.toggle-button :refer [toggle-button]]))

(defn radio-elem []
  (r/as-element [mui-radio]))

(defn combo-radio [value label on-dialog-close]
  (let [radio-elem (radio-elem)]
    (fn [value label on-dialog-close]
      [form-control-label {:value    value
                           :control  radio-elem
                           :label    label
                           :on-click on-dialog-close}])))

(defn combos-dialog [{:keys [open?-ref on-close value-ref on-change]}]
  [dialog {:title     (txt :combos-dialog-title)
           :open?-ref open?-ref
           :on-close  on-close}
   [form-control {:component "fieldset"}
    [radio-group {:value     @value-ref
                  :on-change #(on-change (event/value->keyword %))}
     [combo-radio
      :official
      (r/as-element [:<> (txt :combos-dialog-official-item)
                     [form-helper-text (txt :combos-dialog-official-help)]])
      on-close]
     [combo-radio :all (txt :combos-dialog-all-item) on-close]]]])

(defn dialog-card-item [card <sub-dialog-item-selected?-ref on-dialog-item-selected-change]
  (let [id    (:id card)
        chbox [checkbox {:checked   @(<sub-dialog-item-selected?-ref id)
                         :name      (str id)
                         :on-change #(on-dialog-item-selected-change id (event/checked? %))}]]
    [form-control-label {:control (r/as-element chbox)
                         :label   (:name card)}]))

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
      [box {:mr 1}
       [button-group
        [tooltip (txt-c :select-all)
         [icon-button {:on-click #(on-dialog-selection-set :all)}
          [check-box-icon]]]
        [tooltip (txt-c :clear-selection)
         [icon-button {:on-click #(on-dialog-selection-set :none)}
          [check-box-outline-blank]]]]]
      [search-bar search-str-ref on-search-str-change]]
     [form-control {:component "fieldset"}
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
