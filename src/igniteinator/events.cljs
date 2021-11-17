(ns igniteinator.events
  (:require [igniteinator.db :refer [default-db]]
            [igniteinator.text :refer [txt]]
            [igniteinator.constants :as constants]
            [igniteinator.model.setups :as setups]
            [igniteinator.util.re-frame :refer [reg-event-db-assoc reg-event-db-assoc-store reg-event-set-option
                                                assoc-ins]]
            [clojure.string :as s]
            [re-frame.core :refer [reg-event-fx reg-event-db inject-cofx]]
            [ajax.core :as ajax]))

(reg-event-db-assoc :debug/set-show-card-data)

(reg-event-fx
  :init-db
  [(inject-cofx :query-params)
   (inject-cofx :store)]
  (fn [{:keys [query-params store]} _]
    (let [db (if (or
                   (empty? (:ids query-params))
                   (not (re-matches #"[1-9][0-9]*(,[1-9][0-9]*)*" (:ids query-params))))
               default-db
               (let [ids (map js/parseInt
                           (s/split (:ids query-params) #","))]
                 (assoc-ins default-db
                   [:current-page] :cards
                   [:cards-page :base] ids
                   [:cards-page :card-selection :ids] (set ids))))]
      {:db (-> db
             ;; Don't put language in options map. We often need to access it in events.
             (update :language #(or (:language store) %))
             (update :options #(merge % (:options store))))})))

(reg-event-db-assoc :set-mode)
(reg-event-db-assoc :set-waiting?)

(reg-event-db-assoc :set-language-menu-open?)

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
    (->
      db
      (assoc
        :mode :ready
        :boxes (id-map (:boxes result))
        :types (id-map (:types result))
        :cards (id-map (:cards result))
        :combos-set (:combos result)
        :setups (id-map (:setups result)))
      (assoc-in [:setups-filter :selection]
        (set (map :id (setups/filter-boxes-with-setups
                        (:boxes result)
                        (:setups result))))))))

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
      :waiting? false
      :fatal-message message)))

(reg-event-fx
  :service-worker-ready
  [(inject-cofx :standalone-mode?)]
  (fn [{:keys [db standalone-mode?]} _]
    (if standalone-mode?
      {:post-message [:mode {:mode     :standalone
                             :language (:language db)}]})))

(reg-event-fx
  :set-language
  [(inject-cofx :store)
   (inject-cofx :standalone-mode?)]
  (fn [{:keys [db store standalone-mode?]} [_ lang]]
    (conj
      {:db    (assoc db :language lang)
       :store (assoc store :language lang)}
      (if (and standalone-mode? (not= lang (:language db)))
        {:post-message [:mode {:mode     :standalone
                               :language lang}]}))))

(reg-event-set-option :set-size)

(reg-event-fx
  :reload
  (fn [_ _]
    {:reload []}))

(reg-event-db-assoc :main-menu-mobile/set-open?)

(reg-event-db-assoc :reload-snackbar/set-open?)
(reg-event-fx
  :update-available
  (fn [{:keys [db]} [_ new-sw]]
    {:db (update db :reload-snackbar merge {:open?  true
                                            :new-sw new-sw})}))
(reg-event-fx
  :update-app
  (fn [{:keys [db]}]
    {:db         (assoc db :waiting? true)
     :update-app (get-in db [:reload-snackbar :new-sw])}))

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
        (set val)))))

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

(reg-event-db-assoc :setups-filter/set-operator)
(reg-event-db
  :setups-filter/set-box-selected?
  (fn [db [_ id set?]]
    (let [f (if set? conj disj)]
      (update-in db [:setups-filter :selection] f id))))
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

(reg-event-db-assoc :share/set-dialog-open?)
(reg-event-db-assoc :share/set-snackbar-open?)
(reg-event-db-assoc :share/set-mode)

(reg-event-db-assoc :install-dialog/set-open?)

(reg-event-db-assoc :clear-data/set-dialog-open?)
(reg-event-fx
  :clear-data
  (fn [{:keys [db]}]
    {:db (assoc db :waiting? true)
     ;; Clear local storage, then message service worker to clear cache.
     :fx [[:store {}]
          [:post-message [:clear-data]]]}))
(reg-event-fx
  :data-cleared
  (fn [_ _]
    {:reload nil}))

(reg-event-db-assoc :set-settings-menu-open?)
