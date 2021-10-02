(ns igniteinator.db
  (:require [igniteinator.constants :as constants]
            [re-frame.core :refer [reg-cofx]]))

(def default-db
  {
   :language            constants/default-language
   :mode                :init
   :main-menu-mobile    {:open? false}
   :caching-progress    nil
   :current-page        :cards
   :previous-page       nil
   :size                1
   :install-dialog      {:open? false}
   :select-cards-dialog {:search-str ""}
   :cards-page          {:base           :all
                         :filters        []
                         :sortings       [{:key :name, :order :asc}]
                         :search-str     ""
                         :combos         {:dialog-open? false
                                          :value        :official}
                         :card-selection {:dialog-open? false
                                          :ids          #{}}}
   :card-details-page   {:card-id  nil
                         :sortings [{:key :name, :order :asc}]}
   :setup               {:id nil}
   :card-load-state     {:en nil}
   :cards               nil
   :combos-set          nil})
