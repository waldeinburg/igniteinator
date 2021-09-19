(ns igniteinator.events
  (:require [igniteinator.db :refer [default-db]]
            [igniteinator.text :refer [txt]]
            [igniteinator.constants :as constants]
            [igniteinator.util.re-frame :refer [reg-event-db-assoc assoc-ins]]
            [clojure.string :as s]
            [re-frame.core :refer [reg-event-fx reg-event-db]]
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

(defn- data->cards [data]
  (reduce
    (fn [c v]
      (assoc c (:id v) v))
    {}
    (:cards data)))

(reg-event-db
  :load-data-success
  (fn [db [_ result]]
    (assoc db
      :mode :ready
      :boxes (:boxes result)
      :cards (data->cards result))))

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

(reg-event-db-assoc :caching-progress/set-open?)
(reg-event-db
  :caching-progress/set-progress
  (fn [db [_ {:keys [count progress]}]]
    (assoc db :caching-progress
              {:open?    true
               :count    count
               :progress progress})))

(reg-event-fx
  :page/push
  (fn [{:keys [db]} [_ key]]
    {:db (assoc db
           :current-page key
           :previous-page (:current-page db))
     :fx [[:scroll-to-top]]}))

(reg-event-fx
  :page/pop
  (fn [{:keys [db]} _]
    {:db (if-let [prev (:previous-page db)]
           (assoc db :current-page prev
                     :previous-page nil)
           db)
     :fx [[:scroll-to-top]]}))

(reg-event-db-assoc
  :card-details-page/set-card-id)

(reg-event-fx
  :card-details-page/switch-card
  (fn [_ [_ card]]
    {:fx [[:dispatch [:card-details-page/set-card-id (:id card)]]
          [:scroll-to-top]]}))

(reg-event-fx
  :show-card-details
  (fn [_ [_ card]]
    {:fx [[:dispatch [:card-details-page/set-card-id (:id card)]]
          [:dispatch [:page/push :card-details]]]}))

(reg-event-db-assoc
  :cards-page.card-selection/set-dialog-open?)

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
                :selection (get-in db [:cards-page :card-selection :ids]))]
      (assoc-in db [:cards-page :base] val))))

(reg-event-db-assoc
  :cards-page/set-search-str)

(reg-event-db-assoc
  :install-dialog/set-open?)