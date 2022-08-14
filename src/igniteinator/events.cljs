(ns igniteinator.events
  (:require [ajax.core :as ajax]
            [clojure.string :as s]
            [clojure.string :as str]
            [igniteinator.constants :as constants]
            [igniteinator.db :refer [default-db]]
            [igniteinator.model.setups :as setups]
            [igniteinator.text :refer [txt]]
            [igniteinator.util.re-frame :refer [assoc-db assoc-db-and-store assoc-ins assoc-ins-db
                                                assoc-ins-db-and-store reg-event-db-assoc
                                                reg-event-db-assoc-store reg-event-set-option update-in-db-and-store]]
            [re-frame.core :refer [inject-cofx reg-event-db reg-event-fx]]))

(defn- reg-nav-page-event-set-idx [name root]
  (reg-event-db
    name
    (fn [db [_ idx]]
      (update db root #(assoc % :prev-idx (:idx %)
                                :idx idx
                                :first-transition-in? (not (:first-transition-in? %)))))))
(defn- nav-page-assoc-init [db root idx]
  (assoc-ins db
    [root :idx] idx
    [root :prev-idx] nil
    [root :first-transition-in?] true))

(reg-event-db-assoc :debug/set-show-card-data)

(reg-event-fx
  :init-db
  [(inject-cofx :store)]
  (fn [{:keys [store]} _]
    {:db (-> default-db
           ;; Don't put language in options map. We often need to access it in events.
           (update :language #(or (:language store) %))
           (update :options #(merge % (:options store)))
           (update :epic #(merge % (:epic store))))}))

(reg-event-fx
  :router/start
  (fn [_ _]
    {:fx [[:router/start]]}))

(reg-event-db
  :route
  (fn [db [_ name _ query]]
    (if (and
          (#{:cards :front} name)
          (:ids query)
          (re-matches #"[1-9][0-9]*(,[1-9][0-9]*)*" (:ids query)))
      (let [ids (map js/parseInt
                  (s/split (:ids query) #","))]
        (assoc-ins db
          [:current-page] :cards
          [:cards-page :base] ids
          [:cards-page :card-selection :ids] (set ids)))
      (assoc db :current-page name))))

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

(defn- add-ks-to-boxes [boxes cards]
  (mapv (fn [box]
          (assoc box :ks?
                     (some #(and (:ks %) (= (:id box) (:box %))) cards)))
    boxes))

(defn- default-boxes-setting [boxes]
  (reduce
    (fn [c box]
      (assoc c (:id box)
               (if (:ks? box)
                 :ks
                 true)))
    {}
    boxes))

(defn- load-data-update-db [db result]
  (let [cards (:cards result)
        types (:types result)
        boxes (add-ks-to-boxes (:boxes result) cards)]
    (->
      db
      (assoc
        :mode :ready
        :boxes (id-map boxes)
        :types (id-map types)
        :effects (id-map (:effects result))
        :cards (id-map cards)
        :combos-set (:combos result)
        :setups (id-map (:setups result)))
      (update-in [:options :boxes]
        (fn [boxes-setting]
          (if (= (count boxes-setting) (count boxes))
            boxes-setting
            ;; New boxes or first load. Calculate settings.
            (merge boxes-setting (default-boxes-setting boxes)))))
      (assoc-in [:setups-filter :selection]
        (set (map :id (setups/filter-boxes-with-setups
                        (:boxes result)
                        (:setups result))))))))

(reg-event-fx
  :load-data-success
  [(inject-cofx :store)]
  (fn [{:keys [db store]} [_ result]]
    ;; Db currently contains options loaded from store. If the boxes are updated, update store.
    (let [new-db    (load-data-update-db db result)
          new-store (assoc-in store [:options :boxes]
                      (get-in new-db [:options :boxes]))]
      {:db    new-db
       :store new-store})))

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

;; Box is either false, true or :ks. If selected, alså select ks. Deselecting ks means true instead.
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
(reg-event-fx
  :boxes-setting/set-box?
  (fn [{:keys [db]} [_ box-id on?]]
    (let [ks-box? (-> db :boxes (get box-id) :ks?)]
      {:dispatch [:boxes-setting/set-box?-raw box-id
                  (if on?
                    (if ks-box? :ks true)
                    false)]})))
(reg-event-fx
  :boxes-setting/set-box-ks?
  (fn [_ [_ box-id on?]]
    {:dispatch [:boxes-setting/set-box?-raw box-id (if on? :ks true)]}))

(reg-event-fx
  :boxes-setting/set-box?-raw
  [(inject-cofx :store)]
  (fn [cofx [_ box-id val]]
    (assoc-db-and-store cofx [:options :boxes box-id] val)))
(reg-event-set-option :set-size)
(reg-event-set-option :set-default-order)

(reg-event-set-option :set-display-name?)

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
     :fx [[:router/navigate key]
          [:scroll-to-top]]}))

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

(reg-nav-page-event-set-idx :card-details-page/set-idx :card-details-page)
(reg-event-fx
  :show-card-details
  (fn [{:keys [db]} [_ card-list idx navigate-event]]
    {:db       (-> db
                 (nav-page-assoc-init :card-details-page idx)
                 (assoc-in [:card-details-page :card-ids] (mapv :id card-list)))
     :dispatch [navigate-event :card-details]}))

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
(reg-nav-page-event-set-idx :display-setup-page/set-idx :display-setup-page)
(reg-event-fx
  :display-setup
  (fn [{:keys [db]} [_ idx]]
    {:db       (nav-page-assoc-init db :display-setup-page idx)
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

(reg-event-db-assoc :epic/set-reset-dialog-open?)
(reg-event-db-assoc-store :epic/set-setup-idx)

(reg-event-fx
  :epic/create-game
  [(inject-cofx :store)]
  (fn [cofx
       ;; We don't want to implement stuff like
       ;; https://github.com/den1k/re-frame-utils/blob/master/src/vimsical/re_frame/cofx/inject.cljc
       ;; Instead, require that the component subscribe to :global-cards-unsorted and :epic/setup
       [_ cards setup]]
    (let [count-fn          (:count-fn setup)
          stack-defs        (:stacks setup)
          stacks-with-cards (->>
                              stack-defs
                              ;; Filter
                              (map (fn [stack-def]
                                     (let [stack-cards (filter (:filter stack-def) cards)]
                                       (assoc stack-def :cards stack-cards)))))
          cards-stack-idx   (reduce-kv
                              (fn [csi idx stack]
                                (into csi
                                  (reduce
                                    (fn [m card]
                                      (assoc m (:id card) idx))
                                    {}
                                    (:cards stack))))
                              {}
                              (vec stacks-with-cards))
          stacks            (->>
                              stacks-with-cards
                              ;; Add copies of each card. Cards should only be represented by id to avoid duplicating objects
                              ;; in local storage.
                              (map (fn [stack-def]
                                     (update stack-def :cards
                                       (fn [stack-cards]
                                         (mapcat (fn [card]
                                                   (repeat (count-fn card) (:id card)))
                                           stack-cards)))))
                              ;; Do not keep filter (stacks are stored in local storage)
                              (map #(dissoc % :filter)))]
      ;; The shuffling is non-deterministic and is delegated to an effect which will dispatch the
      ;; :epic/set-stacks event to set the stacks.
      (->
        cofx
        (assoc-ins-db-and-store
          [:epic :active?] true
          [:epic :cards-stack-idx] cards-stack-idx)
        (assoc :epic/shuffle-stacks stacks)))))

(reg-event-fx
  :epic/shuffled-stacks
  [(inject-cofx :store)]
  (fn [cofx [_ stacks]]
    (let [processed-stacks
          (->>
            stacks
            (mapcat
              (fn [stack]
                (if (not (:split? stack))
                  [stack]
                  (loop [sub-stacks (:sub-stacks stack)
                         cards      (:cards stack)
                         res        []]
                    (if (empty? sub-stacks)
                      res
                      (let [[sub-stack & remaining-substacks] sub-stacks
                            take-fn (:take-fn sub-stack)
                            [cards-taken cards-left] (take-fn cards)]
                        (recur remaining-substacks cards-left
                          (conj res (->
                                      sub-stack
                                      (dissoc :take-fn)
                                      (assoc
                                        :description (or (:description sub-stack) (:description stack))
                                        :cards cards-taken))))))))))
            (map-indexed
              (fn [idx stack]
                (assoc stack :idx idx)))
            vec)]
      (assoc-db-and-store cofx [:epic :stacks] processed-stacks))))

(reg-event-fx
  :epic/reset
  [(inject-cofx :store)]
  (fn [cofx _]
    (->
      cofx
      (assoc-ins-db
        [:epic :snackbar-1-message] ""
        [:epic :snackbar-2-message] ""
        [:epic :snackbar-1-open?] true
        [:epic :snackbar-2-open?] false)
      (assoc-ins-db-and-store
        [:epic :active?] false
        [:epic :undo-history] nil
        [:epic :redo-history] nil
        [:epic :cards-stack-idx] nil
        [:epic :stacks] nil
        [:epic :cards-taken] nil))))

(reg-event-db-assoc-store :epic/set-show-stack-info?)

(defn epic-snackbar-message [verb cards card-id preposition stacks stack-idx]
  (let [card  (cards card-id)
        stack (stacks stack-idx)]
    [:<> (str/capitalize verb) " " [:strong (:name card)] " " preposition " " [:strong (:name stack)]]))

(defn epic-event [cofx action card-id stack-idx]
  (let [state (-> cofx :db :epic)]
    {:action    action
     :card-id   card-id
     :stack-idx stack-idx
     :state     (select-keys state
                  [:stacks
                   :cards-taken
                   :cards-stack-idx
                   :snackbar-1-message
                   :snackbar-2-message
                   :snackbar-1-open?
                   :snackbar-2-open?])}))

(defn- add-epic-event [cofx action card-id stack-idx]
  (let [state        (-> cofx :db :epic)
        undo-history (:undo-history state)
        event        (epic-event cofx action card-id stack-idx)]
    (assoc-ins-db-and-store cofx
      [:epic :undo-history] (cons event undo-history)
      [:epic :redo-history] nil)))

(reg-event-fx
  :epic/take-card
  [(inject-cofx :store)]
  (fn [{{:keys                        [cards]
         {:keys [stacks cards-taken]} :epic} :db
        :as                                  cofx}
       [_ stack-idx]]
    (let [card-id (-> stack-idx stacks :cards first)]
      (->
        cofx
        (add-epic-event :take card-id stack-idx)
        (assoc-ins-db-and-store
          [:epic :stacks] (update stacks stack-idx #(update % :cards (comp vec rest)))
          ;; cards-taken holds a map of id -> count
          [:epic :cards-taken] (update cards-taken card-id #(inc (or % 0))))
        (assoc :dispatch [:epic/set-snackbar
                          (epic-snackbar-message "took" cards card-id "from" stacks stack-idx)])))))

(reg-event-fx
  :epic/cycle-card
  [(inject-cofx :store)]
  (fn [{{:keys            [cards]
         {:keys [stacks]} :epic} :db
        :as                      cofx}
       [_ stack-idx]]
    (let [stack-cards (-> stack-idx stacks :cards)
          card-id     (first stack-cards)]
      (->
        cofx
        (add-epic-event :cycle card-id stack-idx)
        (assoc-db-and-store [:epic :stacks stack-idx :cards] (conj (-> stack-cards rest vec) card-id))
        (assoc :dispatch [:epic/set-snackbar
                          (epic-snackbar-message "cycled" cards card-id "in" stacks stack-idx)])))))

(reg-event-db-assoc :epic/set-trashing?)
(reg-event-db-assoc :epic/set-trash-dialog-open?)
(reg-event-db-assoc :epic/set-trash-search-str)

(reg-event-fx
  :epic/trash-card
  [(inject-cofx :store)]
  (fn [{{:keys                                        [cards]
         {:keys [cards-taken cards-stack-idx stacks]} :epic} :db
        :as                                                  cofx}
       [_ card-id]]
    (let [stack-idx (cards-stack-idx card-id)]
      (->
        cofx
        (add-epic-event :trash card-id stack-idx)
        (assoc-ins-db-and-store
          [:epic :stacks]
          (update stacks stack-idx #(update % :cards (fn [stack-cards]
                                                       (conj stack-cards card-id))))
          [:epic :cards-taken]
          (let [cnt (get cards-taken card-id)]
            (if (= 1 cnt)
              (dissoc cards-taken card-id)
              (update cards-taken card-id dec))))
        (assoc :fx [[:dispatch [:epic/close-trash-menu]]
                    [:dispatch [:epic/set-snackbar
                                (epic-snackbar-message "trashed" cards card-id "to" stacks stack-idx)]]])))))

(reg-event-fx
  :epic/set-trash-mode
  [(inject-cofx :store)]
  (fn [{{{:keys [trashing? trash-mode]} :epic} :db :as cofx} [_ new-mode]]
    (if (and trashing? (not= trash-mode new-mode))
      {:fx [[:dispatch [:set-settings-menu-open? false]]
            [:dispatch [:epic/close-trash-menu]]
            [:dispatch [:epic/set-trash-mode new-mode]]
            [:dispatch [:epic/open-trash-menu]]]}
      (assoc-db-and-store cofx [:epic :trash-mode] new-mode))))

(reg-event-fx
  :epic/open-trash-menu
  (fn [{{{:keys [trash-mode]} :epic} :db}]
    ;; Reset search string here to avoid doing it during close animation.
    {:fx [[:dispatch [:epic/set-trash-search-str ""]]
          [:dispatch [:epic/set-trashing? true]]
          [:dispatch (case trash-mode
                       :page [:page/push :epic/trash]
                       :dialog [:epic/set-trash-dialog-open? true])]]}))

(reg-event-fx
  :epic/close-trash-menu
  (fn [{{{:keys [trash-mode]} :epic} :db}]
    {:fx [[:dispatch [:epic/set-trashing? false]]
          [:dispatch (case trash-mode
                       :page [:page/pop]
                       :dialog [:epic/set-trash-dialog-open? false])]]}))

(reg-event-fx
  :epic/set-snackbar
  [(inject-cofx :store)]
  (fn [{{{:keys [snackbar-1-open?]} :epic} :db :as cofx} [_ message]]
    (update-in-db-and-store cofx [:epic]
      (fn [epic]
        (assoc epic
          (if snackbar-1-open? :snackbar-2-message :snackbar-1-message) message
          :snackbar-1-open? (not snackbar-1-open?)          ; If state is false false
          :snackbar-2-open? snackbar-1-open?)))))

(reg-event-fx
  :epic/show-stack
  (fn [{:keys [db]} [_ idx]]
    {:db       (nav-page-assoc-init db :epic-display-stack-page idx)
     :dispatch [:page/push :epic/display-stack]}))
(reg-nav-page-event-set-idx :epic-display-stack-page/set-idx :epic-display-stack-page)
(let [reg-button-event
      (fn [name event]
        (reg-event-fx
          name
          (fn [_ [_ idx]]
            {:fx [[:dispatch [event idx]]
                  [:dispatch [:page/pop]]]})))]
  (reg-button-event :epic-display-stack-page/take-card :epic/take-card)
  (reg-button-event :epic-display-stack-page/cycle-card :epic/cycle-card))

(let [reg-history-event
      (fn [name history-key reverse-history-key]
        (reg-event-fx
          name
          [(inject-cofx :store)]
          (fn [{{:keys [epic]} :db
                :as            cofx}]
            (let [history       (epic history-key)
                  [{:keys [state action card-id stack-idx]} & rest-history] history
                  reverse-event (epic-event cofx action card-id stack-idx)]
              (->
                cofx
                (update-in-db-and-store [:epic] #(merge % state))
                (assoc-db-and-store [:epic history-key] rest-history)
                (update-in-db-and-store [:epic reverse-history-key] #(cons reverse-event %)))))))]
  (reg-history-event :epic/undo :undo-history :redo-history)
  (reg-history-event :epic/redo :redo-history :undo-history))
