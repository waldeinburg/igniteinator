(ns igniteinator.ui.singletons.reload-snackbar
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent.core :as r]
            [reagent-mui.material.snackbar :refer [snackbar]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.icons.cancel :refer [cancel] :rename {cancel cancel-icon}]))

(defn close-reload-snackbar [_ reason]
  (if (not= "clickaway" reason)
    (>evt :reload-snackbar/set-open? false)))

(defn refresh-button []
  (let [updating? (<sub :waiting?)]
    [toolbar
     [button {:variant  :contained
              :disabled updating?
              :color    :primary
              :on-click #(>evt :update-app)}
      (txt :app-update-button)]
     [icon-button {:color      :inherit
                   :disabled   updating?
                   :aria-label :close
                   :on-click   close-reload-snackbar}
      [cancel-icon]]]))

(defn reload-snackbar []
  (let [open? (<sub :reload-snackbar/open?)]
    [snackbar {:open     open?
               :on-close close-reload-snackbar
               :message  (txt :app-update-message)
               :action   (r/as-element [refresh-button])}]))
