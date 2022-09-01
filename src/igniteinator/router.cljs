(ns igniteinator.router
  (:require [bide.core :as r]
            [clojure.string :as s]
            [igniteinator.util.re-frame :refer [>evt assoc-ins]]
            [igniteinator.util.url :refer [to-param-str to-query-array]]))

(def router
  (r/router [["/" :front]
             ["/cards/:card-name" :card-details]
             ["/cards/" :cards]
             ["/setups/" :setups]
             ["/setups/:setup-name" :display-setup]
             ["/epic/" :epic]
             ["/randomizer/" :randomizer]
             ["/randomizer/data/" :randomizer/data]]))

(defn on-navigate [name params query]
  (>evt :route name params query))

(defn start! []
  (r/start! router {:default     :front
                    :on-navigate on-navigate
                    :html5?      true}))

(defn navigate! [name params query]
  (r/navigate! router name params query))

(defn replace! [name params query]
  (r/replace! router name params query))

(defn resolve-to-href
  ([name]
   (r/resolve router name))
  ([name params]
   (r/resolve router name params))
  ([name params query]
   (r/resolve router name params query)))

(defn set-error [db msg]
  (assoc db
    :current-page :error
    :error-message msg))

(defn- ids-from-csv-query [query]
  (if (and
        (:ids query)
        (re-matches #"[1-9][0-9]*(-[1-9][0-9]*)*" (:ids query)))
    (map js/parseInt
      (s/split (:ids query) #"[,-]"))))

(defn- nav-page-state [db root name idx]
  (update db root (fn [state]
                    (if (= name (:current-page db))
                      ;; Navigating inside page
                      (assoc state :prev-idx (:idx state)
                                   :idx idx
                                   :first-transition-in? (not (:first-transition-in? state)))
                      ;; New display
                      (assoc-ins db
                        [root :idx] idx
                        [root :prev-idx] nil
                        [root :first-transition-in?] true)))))

(defn- cards-state [db _ query]
  (let [ids (ids-from-csv-query query)]
    (-> db
      ;; Do not change if ids param is not set
      (update-in [:cards-page :base] #(or ids %))
      (update-in [:cards-page :card-selection :ids] #(or (not-empty (set ids)) %)))))

(defn- card-details-state [db {:keys [card-name]} query]
  (let [=card-name (fn [card]
                     (= card-name (to-param-str (:name card))))
        cards-map  (:cards db)
        q-ids      (ids-from-csv-query query)
        [card-ids idx] (if q-ids
                         (let [cards (map-indexed           ; Not select-keys; we need to keep the order.
                                       (fn [idx id]
                                         (assoc (cards-map id) :idx idx))
                                       q-ids)
                               idx   (->>
                                       cards
                                       (filter =card-name)
                                       first
                                       :idx)]
                           [(mapv :id cards) idx])
                         (let [card (->>
                                      cards-map
                                      (filter (fn [[_ v]]
                                                (=card-name v)))
                                      first
                                      second)]
                           [[(:id card)] 0]))]
    (cond
      (and q-ids (some nil? card-ids)) (set-error db "Invalid id in list")
      (and q-ids (not idx)) (set-error db "Card not found in list")
      (and (not q-ids) (= [nil] card-ids)) (set-error db "Card not found")
      :else (-> db
              (nav-page-state :card-details-page :card-details idx)
              (assoc-ins
                [:card-details-page :card-ids] card-ids
                [:card-details-page :ids-query-str] (to-query-array card-ids))))))

(defn route->state [db name params query]
  (let [state-fn (case name
                   :cards cards-state
                   :card-details card-details-state
                   (fn [db _ _] db))]
    (if (= :ready (:mode db))
      (-> db
        (assoc :current-page name)
        (state-fn params query))
      ;; Postpone state resolving. :load-data-success will dispatch :route.
      (assoc db :init-route [name params query]))))
