(ns igniteinator.events
  (:require [ajax.core :as ajax]
            [clojure.string :as s]
            [igniteinator.constants :as constants]
            [igniteinator.db :refer [default-db]]
            [igniteinator.model.cards :as cards-util]
            [igniteinator.model.setups :as setups]
            [igniteinator.randomizer.randomizer :as randomizer]
            [igniteinator.router :refer [route->state]]
            [igniteinator.subs-calc :refer [default-order-sortings]]
            [igniteinator.text :refer [txt-db]]
            [igniteinator.util.re-frame :refer [assoc-db assoc-db-and-store assoc-ins assoc-ins-db
                                                assoc-ins-db-and-store reg-event-db-assoc
                                                reg-event-db-assoc-store reg-event-set-option update-in-db-and-store]]
            [igniteinator.util.sort :as sort-util]
            [igniteinator.util.url :as url]
            [re-frame.core :refer [inject-cofx reg-event-db reg-event-fx]]))

(defn- reg-nav-page-event-set-idx [name root param-fn query-fn]
  (reg-event-fx
    name
    (fn [{:keys [db]} [_ idx]]
      (let [new-db (update db root #(assoc % :prev-idx (:idx %)
                                             :idx idx
                                             :first-transition-in? (not (:first-transition-in? %))))]
        {:db       new-db
         :dispatch [:page/replace-sub-page (:current-page new-db) (param-fn new-db) (query-fn new-db)]}))))
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

(reg-event-fx
  :route
  (fn [{:keys [db]} [_ name params query]]
    (if (:app-navigating? db)
      ;; The :app-navigating? key is set by our own :page/* events to tell that we have already changed state. This way
      ;; we avoid calculating state from query params all the time.
      {:db (assoc db :app-navigating? false)}
      ;; Not navigating internally.
      (if (and (= :front name) (:ids query))
        ;; Share link. Migrate old style comma query and redirect.
        {:router/replace [:cards nil (update query :ids #(s/replace % #"," "-"))]}
        ;; Just plain navigating. Get state.
        {:db (route->state db name params query)}))))

(reg-event-fx
  :set-site-subtitle
  (fn [_ [_ subtitle]]
    {:set-site-subtitle subtitle}))

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
      {:db       new-db
       :store    new-store
       :dispatch (into [:route] (:init-route new-db))})))

(reg-event-fx
  :load-data-failure
  (fn [{:keys [db]} [_ result]]
    {:dispatch [:fatal
                ;; Language for fatal error will be based on current db state but will not update. That's okay.
                (->
                  (txt-db db :data-load-error)
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
(reg-event-fx
  :update-default-order
  (fn [_ [_ value]]
    {:fx [[:dispatch [:set-default-order value]]
          ;; Recalculate the value the subscription will have.
          ;; TODO: It's probably time to do something about this:
          ;; https://day8.github.io/re-frame/FAQs/UseASubscriptionInAnEventHandler/
          [:dispatch [:randomizer/update-order (default-order-sortings value)]]]}))

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

(defn set-navigating [fx {:keys [db]} name]
  (assoc fx :db (assoc db
                  :app-navigating? true
                  :current-page name)))

(reg-event-fx
  :page/replace
  (fn [cofx _ [_ name params query]]
    (->
      {:router/replace [name params query]}
      (set-navigating cofx name))))

(reg-event-fx
  :page/navigate
  (fn [cofx [_ name params query]]
    (->
      {:fx [[:router/navigate [name params query]]
            [:scroll-to-top]]}
      (set-navigating cofx name))))

(defn- sub-page-query [query back-page scroll-top]
  (assoc query
    :back back-page
    :back-scroll-top scroll-top))

;; Navigate to a sub-page, i.e. a page which have a back button and potentially navigates to other sub-pages which still
;; has the same back-button.
(reg-event-fx
  :page/to-sub-page
  [(inject-cofx :scroll-top)]
  (fn [{{:keys [current-page]} :db
        :keys                  [scroll-top]
        :as                    cofx}
       [_ name params query]]
    (->
      {:fx [[:router/navigate [name params (sub-page-query query current-page scroll-top)]]
            [:scroll-to-top]]}
      (set-navigating cofx name)
      (update :db #(assoc %
                     :back-page current-page
                     :back-scroll-top scroll-top)))))

(reg-event-fx
  :page/to-other-sub-page
  (fn [{{:keys [back-page back-scroll-top]} :db :as cofx} [_ name params query]]
    (->
      {:fx [[:router/navigate [name params (sub-page-query query back-page back-scroll-top)]]
            [:scroll-to-top]]}
      (set-navigating cofx name))))

(reg-event-fx
  :page/replace-sub-page
  (fn [{{:keys [back-page back-scroll-top]} :db :as cofx} [_ name params query]]
    (->
      {:router/replace [name params (sub-page-query query back-page back-scroll-top)]}
      (set-navigating cofx name))))

(reg-event-fx
  :page/back
  (fn [{{:keys [back-page back-scroll-top]} :db :as cofx} _]
    (->
      {:fx [[:router/navigate [back-page]]
            [:scroll-to back-scroll-top]]}
      (set-navigating cofx back-page))))

(reg-event-db
  :set-card-load-state
  (fn [db [_ card state]]
    (let [lang (:language db)
          id   (:id card)]
      (assoc-in db [:card-load-state lang id] state))))

(let [get-params (fn [card]
                   {:card-name (url/to-param-str (:name card))})]
  (reg-nav-page-event-set-idx :card-details-page/set-idx :card-details-page
    (fn [db]
      (let [ids     (get-in db [:card-details-page :card-ids])
            idx     (get-in db [:card-details-page :idx])
            card-id (nth ids idx)]
        (get-params (get (:cards db) card-id))))
    (fn [db]
      {:ids (get-in db [:card-details-page :ids-query-str])}))

  (reg-event-fx
    :show-card-details
    (fn [{:keys [db]} [_ card-list idx navigate-event]]
      (let [params        (get-params (nth card-list idx))
            ids           (mapv :id card-list)
            ids-query-str (url/to-query-array ids)]
        {:db       (-> db
                     (nav-page-assoc-init :card-details-page idx)
                     (assoc-ins
                       [:card-details-page :card-ids] ids
                       [:card-details-page :ids-query-str] ids-query-str))
         :dispatch [navigate-event :card-details params {:ids ids-query-str}]}))))

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
  (fn [db [_ selection]]
    (assoc-in db [:cards-page :card-selection :ids]
      (case selection
        :all (-> db :cards keys set)
        :none #{}
        (set selection)))))

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
  :copy-to-cards-page
  (fn [_ [_ card-ids]]
    {:fx [[:dispatch [:cards-page/reset-filters]]
          [:dispatch [:cards-page.card-selection/set-selection card-ids]]
          [:dispatch [:cards-page/set-base :some]]
          [:dispatch [:page/navigate :cards]]]}))

(reg-event-db-assoc :setups-filter/set-operator)
(reg-event-db
  :setups-filter/set-box-selected?
  (fn [db [_ id set?]]
    (let [f (if set? conj disj)]
      (update-in db [:setups-filter :selection] f id))))

(let [get-params (fn [setup]
                   {:setup-name (url/to-param-str (:name setup))})]
  (reg-nav-page-event-set-idx :display-setup-page/set-idx :display-setup-page
    (fn [db]
      (let [ids      (get-in db [:display-setup-page :setup-ids])
            idx      (get-in db [:display-setup-page :idx])
            setup-id (nth ids idx)]
        (get-params (get (:setups db) setup-id))))
    (fn [db]
      {:ids (get-in db [:display-setup-page :ids-query-str])}))

  (reg-event-fx
    :display-setup
    (fn [{:keys [db]} [_ setups idx]]
      (let [params        (get-params (nth setups idx))
            ids           (mapv :id setups)
            ids-query-str (url/to-query-array ids)]
        {:db       (-> db
                     (nav-page-assoc-init :display-setup-page idx)
                     (assoc-ins
                       [:display-setup-page :setup-ids] ids
                       [:display-setup-page :ids-query-str] ids-query-str))
         :dispatch [:page/to-sub-page :display-setup params {:ids ids-query-str}]}))))

(reg-event-fx
  :current-setup/copy-to-cards-page
  (fn [{{{:keys [idx setup-ids]} :display-setup-page :as db} :db} _]
    (let [current-setup-id (get setup-ids idx)
          card-ids         (get-in db [:setups current-setup-id :cards])]
      {:dispatch [:copy-to-cards-page card-ids]})))

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
    [:<> (s/capitalize verb) " " [:strong (:name card)] " " preposition " " [:strong (:name stack)]]))

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
  (fn [{{{:keys [trash-mode]} :epic} :db} _]
    ;; Reset search string here to avoid doing it during close animation.
    {:fx [[:dispatch [:epic/set-trash-search-str ""]]
          [:dispatch [:epic/set-trashing? true]]
          [:dispatch (case trash-mode
                       :page [:page/navigate :epic/trash]
                       :dialog [:epic/set-trash-dialog-open? true])]]}))

(reg-event-fx
  :epic/close-trash-menu
  (fn [{{{:keys [trash-mode]} :epic} :db}]
    {:fx [[:dispatch [:epic/set-trashing? false]]
          [:dispatch (case trash-mode
                       :page [:page/back]
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
     :dispatch [:page/to-sub-page :epic/display-stack]}))
(reg-nav-page-event-set-idx :epic-display-stack-page/set-idx :epic-display-stack-page
  (constantly nil)
  (constantly nil))
(let [reg-button-event
      (fn [name event]
        (reg-event-fx
          name
          (fn [_ [_ idx]]
            {:fx [[:dispatch [event idx]]
                  [:dispatch [:page/back]]]})))]
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

;; Do not store. User will probably want these to reset.
(reg-event-db-assoc :randomizer/set-edit?)
(reg-event-db-assoc :randomizer/set-changed?)
(reg-event-db-assoc :randomizer/set-replace-using-specs?)
(reg-event-db-assoc :randomizer/set-show-specs?)
(reg-event-db-assoc :randomizer/set-reset-dialog-open?)

(reg-event-db
  :randomizer/reset
  (fn [db _]
    (assoc-in db [:randomizer :selected-cards] nil)))

(reg-event-fx
  :randomizer/generate-market
  (fn [_ [_ filter-utils specs card-ids]]
    {:dispatch                                     [:randomizer/set-edit? false]
     :randomizer/shuffle-cards-and-generate-market [filter-utils specs card-ids]}))

(reg-event-db
  :randomizer/generate-market-from-shuffled-ids
  (fn [{:keys [cards] :as db} [_ filter-utils specs shuffled-card-ids]]
    (let [shuffled-cards (map cards shuffled-card-ids)
          [selected-cards cards-left title-cards-left] (randomizer/generate-market filter-utils shuffled-cards specs)
          card-ids-left  (mapv :id cards-left)
          title-ids-left (mapv :id title-cards-left)]
      (update db :randomizer #(assoc %
                                :selected-cards selected-cards
                                :card-ids-left card-ids-left
                                :title-ids-left title-ids-left)))))

(reg-event-fx
  :randomizer/replace-card
  (fn [{{{:keys [selected-cards card-ids-left title-ids-left replace-using-specs?]} :randomizer
         :keys                                                                      [cards] :as db} :db}
       [_ filter-utils specs idx-to-replace]]
    {:dispatch [:randomizer/set-changed? true]
     :db
     (let [cards-left         (map cards card-ids-left)
           title-cards-left   (map cards title-ids-left)
           [new-selected-cards new-cards-left new-title-cards-left]
           (randomizer/replace-selected-card
             filter-utils replace-using-specs? specs selected-cards cards-left title-cards-left idx-to-replace)
           new-card-ids-left  (mapv :id new-cards-left)
           new-title-ids-left (mapv :id new-title-cards-left)]
       (update db :randomizer #(assoc %
                                 :selected-cards new-selected-cards
                                 :card-ids-left new-card-ids-left
                                 :title-ids-left new-title-ids-left)))}))

(reg-event-fx
  :randomizer/edit-start
  (fn [_ [_ default-sortings]]
    {:fx [[:dispatch [:randomizer/set-edit? true]]
          [:dispatch [:randomizer/recalculate-order default-sortings]]]}))

(reg-event-fx
  :randomizer/edit-done
  (fn [_ _]
    {:dispatch [:randomizer/set-edit? false]}))

(reg-event-fx
  :randomizer/update-show-specs?
  (fn [_ [_ default-sortings value]]
    {:fx [[:dispatch [:randomizer/set-show-specs? value]]
          [:dispatch [:randomizer/recalculate-order default-sortings]]]}))

(reg-event-fx
  :randomizer/update-order
  (fn [_ [_ default-sortings]]
    {:fx [[:dispatch [:randomizer/set-changed? false]]
          [:dispatch [:randomizer/recalculate-order default-sortings]]]}))

(reg-event-fx
  :randomizer/recalculate-order
  (fn [{{{:keys [edit? show-specs?]} :randomizer :as db} :db} [_ default-sortings]]
    {:dispatch [:randomizer/set-changed? false]
     :db
     (if (and edit? (not show-specs?))
       (update-in db [:randomizer :selected-cards]
         (fn [selected-cards]
           (let [new-order (->> selected-cards
                             (sort-util/sort-by-hierarchy (cards-util/sorting-specs->comparators default-sortings))
                             (map-indexed vector))]
             (mapv (fn [card]
                     (let [id (:id card)
                           [idx _] (first (filter (fn [[_ c]]
                                                    (= id (:id c)))
                                            new-order))]
                       (assoc card :order-idx idx)))
               selected-cards))))
       db)}))

(reg-event-fx
  :randomizer/copy-to-cards-page
  (fn [{{{:keys [selected-cards]} :randomizer} :db} _]
    (let [card-ids (mapv :id selected-cards)]
      {:dispatch [:copy-to-cards-page card-ids]})))
