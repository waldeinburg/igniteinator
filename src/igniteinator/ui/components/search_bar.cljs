(ns igniteinator.ui.components.search-bar
  (:require [igniteinator.util.event :as event]
            [igniteinator.text :refer [txt-c txt]]
            [igniteinator.constants :as const]
            [igniteinator.util.string :as ss]
            [igniteinator.ui.components.link :refer [external-link]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent.core :as r]
            [reagent-mui.material.text-field :refer [text-field]]
            [reagent-mui.material.input-adornment :refer [input-adornment]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.icons.search :refer [search] :rename {search search-icon}]
            [reagent-mui.icons.clear :refer [clear] :rename {clear clear-icon}]))

(defn clear-search-bar [input-ref on-change]
  (set! (.-value @input-ref))
  (on-change ""))

(defn search-and-clear-icon [input-ref search-str-ref on-change]
  (r/as-element
    [input-adornment {:position :end}
     (if (empty? @search-str-ref)
       [icon-button {:disabled true} [search-icon]]
       [icon-button {:on-click #(clear-search-bar input-ref on-change)}
        [clear-icon]])]))

(defn regular-expressions-helper-text-elem []
  [:<> (txt-c :using) " "
   [external-link const/regular-expressions-site (txt :regular-expressions)]])

(defn search-bar [search-str-ref on-change options]
  (let [input-ref (r/atom nil)]
    (fn [search-str-ref on-change {:keys [placeholder]
                                   :or   {placeholder (str (txt-c :search) " …")}}]
      (let [re-help    (regular-expressions-helper-text-elem)
            search-str @search-str-ref
            re?        (and
                         (re-find #"[.*+?{}()\[\]^$\|\\]" search-str)
                         (ss/re-pattern-no-error search-str))]
        [text-field {:variant       :standard
                     :input-ref     #(reset! input-ref %)
                     :default-value search-str              ; not :value (will cause rerendering on input)
                     :placeholder   placeholder
                     :on-key-down   #(when (= "Escape" (.-key %))
                                       (clear-search-bar input-ref on-change)
                                       ;; Prevent dialogs from closing on escape.
                                       (.stopPropagation %))
                     :on-change     (fn [ev]
                                      (-> ev event/value on-change))
                     :helper-text   (if re? (r/as-element re-help))
                     :InputProps    {:end-adornment (search-and-clear-icon input-ref search-str-ref on-change)}}]))))
