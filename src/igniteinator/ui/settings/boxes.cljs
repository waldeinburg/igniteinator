(ns igniteinator.ui.settings.boxes
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.bool-input :refer [checkbox]]
            [igniteinator.ui.components.form-item :refer [form-item]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [reagent-mui.material.box :refer [box]]))

(defn boxes-settings []
  (let [boxes (<sub :all-boxes)]
    [form-item {:label (txt :settings.boxes/label)}
     [box {:component :ul
           :sx        {:list-style :none
                       :p          0}}
      (add-children
        (for [b boxes]
          (let [id (:id b)]
            [box {:component :li}
             [checkbox {:checked?-ref (<sub-ref :boxes-setting/box? b)
                        :label        (:name b)
                        :disabled     (= 1 (:id b))
                        :on-change    #(>evt :boxes-setting/set-box? id %)}]
             (if (:ks? b)
               [box {:component :ul
                     :sx        {:list-style :none
                                 :pl         4}}
                [box {:component :li
                      :sx        {:mb 1}}
                 [checkbox {:checked?-ref (<sub-ref :boxes-setting/box-ks? b)
                            :label        (txt :settings.boxes/ks)
                            :disabled     (not (:ks? b))
                            :on-change    #(>evt :boxes-setting/set-box-ks? id %)}]]])])))]]))
