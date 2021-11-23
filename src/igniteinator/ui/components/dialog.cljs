(ns igniteinator.ui.components.dialog
  (:require [igniteinator.text :refer [txt-c]]
            [igniteinator.util.reagent :refer [add-children]]
            [reagent-mui.material.dialog :refer [dialog] :rename {dialog mui-dialog}]
            [reagent-mui.material.dialog-title :refer [dialog-title]]
            [reagent-mui.material.dialog-content :refer [dialog-content]]
            [reagent-mui.material.dialog-actions :refer [dialog-actions]]
            [reagent-mui.material.button :refer [button]]))

;; Dialog will resize when content is dynamically changed (e.g. in the card selection dialog when
;; filtering by name. Fix dialog at the top instead of center.
;; https://stackoverflow.com/a/61094451
#_(def dialog-at-top
  ((styles/with-styles {:scroll-paper {:align-items :baseline}}) mui-dialog))
(def dialog-at-top mui-dialog)

(defn dialog [{:keys [title button-text open?-ref top? on-close buttons]
               :or   {top? false, button-text (txt-c :ok)}
               :as   props}
              & children]
  (let [dialog-type  (if top? dialog-at-top mui-dialog)
        open?        @open?-ref
        dialog-props (into {:open open?} (dissoc props :open?-ref :top? :button-text))]
    [dialog-type dialog-props
     [dialog-title title]
     [dialog-content (add-children children)]
     [dialog-actions
      (if buttons
        (add-children buttons)
        [button {:on-click on-close} button-text])]]))
