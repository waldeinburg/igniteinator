(ns igniteinator.events
  (:require [igniteinator.db :refer [default-db]]
            [igniteinator.text :refer [txt]]
            [igniteinator.constants :as constants]
            [igniteinator.util.re-frame :refer [reg-event-db-assoc assoc-ins]]
            [clojure.string :as s]
            [re-frame.core :refer [reg-event-fx reg-event-db inject-cofx]]
            [ajax.core :as ajax]))

(reg-event-db
  :init-db
  (fn [_ _]
    default-db))

(reg-event-fx
  :load-data
  (fn [{:keys [db]} _]
    {:db         (assoc db :mode :loading)
     :http-xhrio {:method          :get
                  :uri             constants/data-file-path
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:load-data-success]
                  :on-failure      [:load-data-failure]}}))

(defn- id-map [coll]
  (reduce
    (fn [c v]
      (assoc c (:id v) v))
    {}
    coll))

(reg-event-db
  :load-data-success
  (fn [db [_ result]]
    (assoc db
      :mode :ready
      :boxes (id-map (:boxes result))
      :cards (id-map (:cards result))
      :combos-set (:combos result)
      :setups (id-map (:setups result)))))

(reg-event-fx
  :load-data-failure
  (fn [{:keys [db]} [_ result]]
    {:dispatch [:fatal
                ;; Language for fatal error will be based on current db state but will not update. That's okay.
                (->
                  (:language db)
                  (txt :data-load-error)
                  (s/capitalize)
                  (str ":" (:status-text result)))]}))

(reg-event-db
  :fatal
  (fn [db [_ message]]
    (assoc db
      :mode :fatal-error
      :fatal-message message)))

(reg-event-db-assoc :main-menu-mobile/set-open?)

(reg-event-db-assoc :caching-progress/set-open?)
(reg-event-db
  :caching-progress/set-progress
  (fn [db [_ {:keys [count progress]}]]
    (assoc db :caching-progress
              {:open?    true
               :count    count
               :progress progress})))

(reg-event-fx
  :scroll-to
  (fn [_ [_ n]]
    {:fx [[:scroll-to n]]}))

;; Push page onto history stack.
(reg-event-fx
  :page/push
  [(inject-cofx :scroll-top)]
  (fn [{:keys [db scroll-top]} [_ key]]
    {:db (-> db
           (assoc :current-page key)
           (update :page-history conj {:page       (:current-page db)
                                       :scroll-top scroll-top}))
     :fx [[:scroll-to-top]]}))

;; Replace current page, not changing the history stack.
(reg-event-fx
  :page/replace
  [(inject-cofx :scroll-top)]
  (fn [{:keys [db]} [_ key]]
    {:db (assoc db :current-page key)
     :fx [[:scroll-to-top]]}))

;; Set current page, clearing the history stack.
(reg-event-fx
  :page/set
  [(inject-cofx :scroll-top)]
  (fn [{:keys [db]} [_ key]]
    {:db (assoc db :current-page key
                   :page-history [])
     :fx [[:scroll-to-top]]}))

;; Pop page from history stack.
(reg-event-fx
  :page/pop
  (fn [{:keys [db]} _]
    (let [hist (:page-history db)]
      (if-let [{:keys [page scroll-top]} (first hist)]
        {:db             (assoc db :current-page page
                                   :page-history (rest hist))
         ;; Don't use the fx directly but dispatch an event after a delay to let React rerender before scrolling.
         ;; TODO: Create a non-race-condition method for this.
         :dispatch-later {:ms 100 :dispatch [:scroll-to scroll-top]}}))))

(reg-event-db
  :set-card-load-state
  (fn [db [_ card state]]
    (let [lang (:language db)
          id   (:id card)]
      (assoc-in db [:card-load-state lang id] state))))

(reg-event-db-assoc
  :card-details-page/set-card-id)

(reg-event-fx
  :show-card-details
  (fn [_ [_ card navigate-event]]
    {:fx [[:dispatch [:card-details-page/set-card-id (:id card)]]
          [:dispatch [navigate-event :card-details]]]}))

(reg-event-db-assoc
  :cards-page.combos/set-dialog-open?)
(reg-event-db-assoc
  :cards-page.combos/set-value)
(reg-event-db-assoc
  :cards-page.card-selection/set-dialog-open?)
(reg-event-db-assoc
  :select-cards-dialog/set-search-str)

(reg-event-db
  :cards-page.card-selection/set-selection
  (fn [db [_ val]]
    (assoc-in db [:cards-page :card-selection :ids]
      (case val
        :all (-> db :cards keys set)
        :none #{}
        val))))

(reg-event-db
  :cards-page.card-selection/set-item-selected?
  (fn [db [_ id set?]]
    (let [f (if set? conj disj)]
      (update-in db [:cards-page :card-selection :ids] f id))))

(reg-event-db
  :cards-page/set-base
  (fn [db [_ key]]
    (let [val (case key
                :all :all
                :combos :combos
                :some (get-in db [:cards-page :card-selection :ids]))]
      (assoc-in db [:cards-page :base] val))))

(reg-event-db-assoc
  :cards-page/set-search-str)

(reg-event-fx
  :cards-page/reset-filters
  (fn [{:keys [db]} _]
    {:db       (assoc-in db [:cards-page :filters] [])
     :dispatch [:cards-page/set-search-str ""]}))

(reg-event-fx
  :display-setup
  (fn [{:keys [db]} [_ id]]
    {:db       (assoc-in db [:setup :id] id)
     :dispatch [:page/push :display-setup]}))

(reg-event-fx
  :current-setup/copy-to-cards-page
  (fn [{:keys [db]} _]
    (let [current-setup-id (get-in db [:setup :id])]
      {:fx [[:dispatch [:cards-page.card-selection/set-selection
                        (get-in db [:setups current-setup-id :cards])]]
            [:dispatch [:cards-page/set-base :some]]
            [:dispatch [:cards-page/reset-filters]]
            [:dispatch [:page/set :cards]]]})))

(reg-event-db-assoc
  :install-dialog/set-open?)