(ns igniteinator.ui.settings.default-order
  (:require [igniteinator.util.re-frame :refer [<sub-ref >evt]]
            [igniteinator.text :refer [txt txt-c]]
            [clojure.string :as s]
            [igniteinator.ui.components.radio-group :refer [radio-group radio]]))

(defn default-order-settings []
  (let [name-str     (txt :name)
        name-cap-str (s/capitalize name-str)
        cost-cap-str (txt-c :cost)]
    [radio-group {:value-ref (<sub-ref :default-order)
                  :on-change #(>evt :set-default-order %)
                  :label     (txt :settings.default-order/label)}
     [radio {:value :cost-name
             :label (str cost-cap-str ", " name-str)}]
     [radio {:value :name
             :label name-cap-str}]]))
