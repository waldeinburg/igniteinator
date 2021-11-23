(ns igniteinator.ui.singletons.language-menu
  (:require [igniteinator.constants :as constants]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.ui.components.menu-button :refer [menu-button]]
            [reagent-mui.icons.translate :refer [translate] :rename {translate translate-icon}]
            [reagent-mui.material.menu-item :refer [menu-item]]
            [clojure.string :as s]))

(defn language-menu []
  (let [lang     (<sub :language)
        on-close #(>evt :set-language-menu-open? false)]
    [menu-button {:button-id     :language-button
                  :menu-id       :language-menu
                  :open?-sub     :language-menu-open?
                  :set-open?-evt :set-language-menu-open?
                  :tooltip-key   :language-button-tooltip
                  :icon          translate-icon}
     (for [l constants/languages]
       (let [{:keys [id name]} l
             active? (= lang id)]
         ^{:key id}
         [menu-item {:selected active?
                     :on-click (if active?
                                 on-close
                                 (fn []
                                   (on-close)
                                   (>evt :set-language id)))}
          (s/capitalize name)]))]))
