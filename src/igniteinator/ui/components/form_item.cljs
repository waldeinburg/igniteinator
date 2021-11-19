(ns igniteinator.ui.components.form-item
  (:require [igniteinator.util.reagent :refer [add-children]]
            [reagent-material-ui.core.form-control :refer [form-control]]
            [reagent-material-ui.core.form-label :refer [form-label]]))

(defn form-item [{:keys [label] :as props} & children]
  [form-control (into {:component :fieldset}
                  (dissoc props :label))
   (if label
     [form-label {:component :legend} label])
   (add-children children)])
