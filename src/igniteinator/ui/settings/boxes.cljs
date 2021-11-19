(ns igniteinator.ui.settings.boxes
  (:require [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [igniteinator.ui.components.checkbox :refer [checkbox]]
            [reagent-material-ui.core.box :refer [box]]
            [reagent-material-ui.core.form-group :refer [form-group]]))

(defn boxes-settings []
  (let [boxes (<sub :all-boxes)]
    [form-item {:label (txt :settings.boxes/label)}
     (add-children
       (for [b boxes]
         (let [id (:id b)]
           [form-group {:row true}
            [checkbox {:checked?-ref (<sub-ref :boxes-setting/box? b)
                       :label        (:name b)
                       :disabled     (= 1 (:id b))
                       :on-change    #(>evt :boxes-setting/set-box? id %)}]
            [box {:ml :auto}
             [checkbox {:checked?-ref (<sub-ref :boxes-setting/box-ks? b)
                        :label        (txt :settings.boxes/ks)
                        :disabled     (not (:ks? b))
                        :on-change    #(>evt :boxes-setting/set-box-ks? id %)}]]])))]))
