(ns igniteinator.ui.search-bar
  (:require [igniteinator.util.event :as event]
            [igniteinator.text :refer [txt-c txt]]
            [igniteinator.constants :as const]
            [igniteinator.util.string :as ss]
            [igniteinator.ui.link :refer [external-link]]
            [reagent.core :as r]
            [reagent-material-ui.core.text-field :refer [text-field]]
            [reagent-material-ui.core.input-adornment :refer [input-adornment]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.search :refer [search] :rename {search search-icon}]
            [reagent-material-ui.icons.clear :refer [clear] :rename {clear clear-icon}]))

(defn search-and-clear-icon [input-ref search-str-atom]
  (r/as-element
    [input-adornment {:position :end}
     (if (empty? @search-str-atom)
       [icon-button {:disabled true} [search-icon]]
       [icon-button {:on-click #(do (set! (.-value @input-ref))
                                    (reset! search-str-atom ""))}
        [clear-icon]])]))

(def regular-expressions-helper-text-elem
  (r/as-element
    [:<> (txt-c :using) " "
     [external-link const/regular-expressions-site (txt :regular-expressions)]]))

(defn search-bar [_ _]
  (let [input-ref (r/atom nil)]
    (fn [search-str-atom {:keys [placeholder on-change]
                          :or   {placeholder (str (txt-c :search) " …"), on-change #()}}]
      (let [re? (and
                  (re-find #"[.*+?{}()\[\]^$\|\\]" @search-str-atom)
                  (ss/re-pattern-no-error @search-str-atom))]
        [text-field {:input-ref     #(reset! input-ref %)
                     :default-value @search-str-atom        ; not :value (will cause rerendering on input)
                     :placeholder   placeholder
                     :on-change     (fn [ev]
                                      (let [v (event/value ev)]
                                        (reset! search-str-atom v)
                                        (on-change v)))
                     :helper-text   (when re? regular-expressions-helper-text-elem)
                     :InputProps    {:end-adornment (search-and-clear-icon input-ref search-str-atom)}}]))))
