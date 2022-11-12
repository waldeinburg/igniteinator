(ns igniteinator.db
  (:require [igniteinator.constants :as constants]))

(def default-db
  {
   :app-navigating?         false
   :language                constants/default-language
   :options                 {:boxes         nil             ; set when loading
                             :size          1,
                             :default-order :cost-name
                             :display-name? :translating}
   :debug                   {:show-card-data false}
   :mode                    :init
   :waiting?                false
   :main-menu-mobile        {:open? false}
   :reload-snackbar         {:open?  false
                             :new-sw nil}
   :caching-progress        nil
   :current-page            :front
   :back-page               nil
   :back-scroll-top         nil
   :language-menu-open?     false
   :settings-menu-open?     false
   :share                   {:dialog-open?   false
                             :snackbar-open? false
                             :mode           :url}
   :install-dialog          {:open? false}
   :clear-data              {:dialog-open? false}
   :select-cards-dialog     {:search-str ""}
   :cards-page              {:base           :all
                             :filters        []
                             :sortings       nil
                             :search-str     ""
                             :combos         {:dialog-open? false
                                              :value        :official}
                             :card-selection {:dialog-open? false
                                              :ids          #{}}}
   :card-details-page       {:idx                  nil
                             :prev-idx             nil
                             :first-transition-in? true
                             :card-ids             nil
                             :ids-query-str        nil
                             :sortings             nil}
   :setups-filter           {:operator  :some
                             :selection nil}                ; filled when data is loaded
   :display-setup-page      {:idx                  nil
                             :prev-idx             nil
                             :first-transition-in? true
                             :setup-ids            nil
                             :ids-query-str        nil
                             :sortings             nil}
   :epic                    {:setups             nil
                             :show-stack-info?   false
                             :reset-dialog-open? false
                             :active?            false
                             :undo-history       nil
                             :redo-history       nil
                             :setup-idx          0
                             :stacks             nil
                             :cards-taken        nil
                             :cards-stack-idx    nil
                             :trashing?          false
                             :trash-mode         :page
                             :trash-dialog-open? false
                             :trash-search-str   ""
                             :snackbar-1-message ""
                             :snackbar-2-message ""
                             :snackbar-1-open?   true
                             :snackbar-2-open?   false}
   ;; Must be in root, not under :epic (and not with "epic."), for nav-page functions to work.
   :epic-display-stack-page {:idx                  nil
                             :prev-idx             nil
                             :first-transition-in? true}
   :randomizer              {:cards-base           :all
                             :selected-cards       nil      ; cards with metadata, not ids
                             :card-ids-left        nil
                             :title-ids-left       nil
                             :replace-using-specs? true
                             :edit?                true}
   :card-load-state         {:en nil}
   :cards                   nil
   :types                   nil
   :effects                 nil
   :combos-set              nil})
