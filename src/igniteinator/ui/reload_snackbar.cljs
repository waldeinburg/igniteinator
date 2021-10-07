(ns igniteinator.ui.reload-snackbar
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [<sub >evt]]
            [reagent.core :as r]
            [reagent-material-ui.core.snackbar :refer [snackbar]]
            [reagent-material-ui.core.button :refer [button]]
            [reagent-material-ui.core.toolbar :refer [toolbar]]
            [reagent-material-ui.core.icon-button :refer [icon-button]]
            [reagent-material-ui.icons.cancel :refer [cancel] :rename {cancel cancel-icon}]))

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
