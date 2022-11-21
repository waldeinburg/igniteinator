(ns igniteinator.ui.components.link
  (:require [igniteinator.router :refer [resolve-to-href]]
            [igniteinator.util.event :refer [link-on-click]]
            [igniteinator.util.re-frame :refer [>evt]]
            [reagent-mui.material.link :refer [link]]))

(defn external-link [href & children]
  [link {:href   href
         :target "_blank"
         :rel    :noreferrer}
   children])

(defn internal-link [name {:keys [navigate-event component]
                           :or   {navigate-event :page/navigate
                                  component      link}
                           :as   props}
                     & children]
  (let [href (resolve-to-href name)]
    [component (merge {:href     href
                       :on-click (link-on-click #(>evt navigate-event name))}
                 (dissoc props :nagivate-event :component))
     children]))
