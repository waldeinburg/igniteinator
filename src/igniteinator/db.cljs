(ns igniteinator.db
  (:require [igniteinator.constants :as constants]
            [re-frame.core :refer [reg-cofx]]))

(def default-db
  {
   :language            constants/default-language
   :options             {:size          1,
                         :default-order :cost-name
                         :display-name? :translating}
   :debug               {:show-card-data false}
   :mode                :init
   :waiting?            false
   :main-menu-mobile    {:open? false}
   :reload-snackbar     {:open?  false
                         :new-sw nil}
   :caching-progress    nil
   :current-page        :cards
   :page-history        []
   :language-menu-open? false
   :settings-menu-open? false
   :share               {:dialog-open?   false
                         :snackbar-open? false
                         :mode           :url}
   :install-dialog      {:open? false}
   :clear-data          {:dialog-open? false}
   :select-cards-dialog {:search-str ""}
   :cards-page          {:base           :all
                         :filters        []
                         :sortings       nil
                         :search-str     ""
                         :combos         {:dialog-open? false
                                          :value        :official}
                         :card-selection {:dialog-open? false
                                          :ids          #{}}}
   :card-details-page   {:card-id  nil
                         :sortings nil}
   :setups-filter       {:operator  :some
                         :selection nil}                    ; filled when data is loaded
   :setup               {:id nil}
   :display-setup-page  {:sortings nil}
   :card-load-state     {:en nil}
   :cards               nil
   :combos-set          nil})
