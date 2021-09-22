(ns igniteinator.db
  (:require [igniteinator.constants :as constants]
            [re-frame.core :refer [reg-cofx]]))

(def default-db
  {
   :language            constants/default-language
   :mode                :init
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
                         :card-selection {:dialog-open? false
                                          :ids          #{}}}
   :card-details-page   {:card-id  nil
                         :sortings [{:key :name, :order :asc}]}
   :cards               nil})
