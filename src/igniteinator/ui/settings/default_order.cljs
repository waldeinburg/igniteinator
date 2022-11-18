(ns igniteinator.ui.settings.default-order
  (:require [clojure.string :as s]
            [igniteinator.text :refer [txt txt-c]]
            [igniteinator.ui.components.radio-group :refer [radio radio-group]]
            [igniteinator.util.re-frame :refer [<sub-ref >evt]]))

(defn default-order-settings []
  (let [name-str     (txt :name)
        name-cap-str (s/capitalize name-str)
        cost-cap-str (txt-c :cost)]
    [radio-group {:value-ref (<sub-ref :default-order)
                  :on-change #(>evt :update-default-order %) ; not :set-default-order (cf. event)
                  :label     (txt :settings.default-order/label)}
     [radio {:value :cost-name
             :label (str cost-cap-str ", " name-str)}]
     [radio {:value :name
             :label name-cap-str}]]))
