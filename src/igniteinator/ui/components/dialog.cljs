(ns igniteinator.ui.components.dialog
  (:require [igniteinator.text :refer [txt-c]]
            [igniteinator.util.reagent :refer [add-children]]
            [reagent-mui.material.dialog :refer [dialog] :rename {dialog mui-dialog}]
            [reagent-mui.material.dialog-title :refer [dialog-title]]
            [reagent-mui.material.dialog-content :refer [dialog-content]]
            [reagent-mui.material.dialog-actions :refer [dialog-actions]]
            [reagent-mui.material.button :refer [button]]))

(defn dialog [{:keys [title button-text button-color open?-ref top? on-close button-props buttons]
               :or   {top? false, button-text (txt-c :ok), button-color :primary}
               :as   props}
              & children]
  (let [open?        @open?-ref
        dialog-props (into {:open open?
                            ;; Dialog will resize when content is dynamically changed (e.g. in the card selection dialog
                            ;; when filtering by name. Fix dialog at the top instead of center.
                            ;; https://stackoverflow.com/a/61094451
                            :sx   (if top? {"& .MuiDialog-container" {:align-items :baseline}})}
                       (dissoc props :open?-ref :top? :button-text :button-color))]
    [mui-dialog dialog-props
     [dialog-title title]
     [dialog-content (add-children children)]
     [dialog-actions
      (if buttons
        (add-children buttons)
        [button (merge {:on-click on-close, :variant :contained, :color button-color} button-props)
         button-text])]]))
