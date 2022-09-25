(ns igniteinator.ui.components.page
  (:require [igniteinator.util.re-frame :refer [>evt]]
            [igniteinator.util.reagent :refer [add-children destruct-props-children]]
            [reagent.core :as r]))

(defn- set-site-subtitle [this]
  (let [[_ site-subtitle] (r/children this)]
    (>evt :set-site-subtitle site-subtitle)))

(defn page-title [title]
  [:h2 title])

(defn page-title-and-set-site-subtitle [title site-subtitle]
  (if (not= :keep site-subtitle)
    ;; Well, the (functionally) clean solution would be a mess.
    (r/create-class
      {:component-did-mount  set-site-subtitle
       :component-did-update set-site-subtitle
       :render               (fn [this]
                               (let [[title] (r/children this)]
                                 (page-title title)))})
    (page-title title)))

(defn page [title & children]
  (let [[props children] (destruct-props-children children)
        {:keys [site-subtitle] :or {site-subtitle title}} props]
    [:<>
     [page-title-and-set-site-subtitle title site-subtitle]
     (add-children children)]))
