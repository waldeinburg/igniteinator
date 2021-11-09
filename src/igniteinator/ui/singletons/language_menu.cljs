(ns igniteinator.ui.singletons.language-menu
  (:require [igniteinator.constants :as constants]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [igniteinator.util.reagent :refer [add-children]]
            [igniteinator.text :refer [txt]]
            [igniteinator.ui.components.tooltip :refer [tooltip]]
            [goog.dom :as gdom]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.translate :refer [translate] :rename {translate translate-icon}]
            [reagent-material-ui.core.menu :refer [menu]]
            [reagent-material-ui.core.menu-item :refer [menu-item]]
            [clojure.string :as s]))

(defn language-menu []
  (let [open?    (<sub :language-menu-open?)
        lang     (<sub :language)
        on-close #(>evt :set-language-menu-open? false)]
    [:<>
     [tooltip (txt :language-button-tooltip)
      [icon-button {:id            :language-button
                    :aria-controls :language-menu
                    :aria-haspopup true
                    :aria-expanded open?
                    :on-click      #(>evt :set-language-menu-open? true)}
       [translate-icon]]]
     [menu {:id            :language-menu
            :open          open?
            :on-close      on-close
            :anchor-el     #(gdom/getElement (name :language-button))
            :MenuListProps {:aria-labelledby :language-button}}
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
           (s/capitalize name)]))]]))
