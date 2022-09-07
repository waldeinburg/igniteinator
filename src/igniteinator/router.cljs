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

(defn- back-button-state [db query]
  (assoc db :back-page (keyword (:back query))
            :back-scroll-top (js/parseInt (:back-scroll-top query))))

(defn- ids-from-csv-query [query]
  (if (and
        (:ids query)
        (re-matches #"(0|[1-9][0-9]*)(-(0|[1-9][0-9]*))*" (:ids query)))
    (map js/parseInt
      (s/split (:ids query) #"[,-]"))))

(defn- nav-page-state-set-idx [db root page-key idx]
  (update db root (fn [state]
                    (if (= page-key (:current-page db))
                      ;; Navigating inside page
                      (assoc state :prev-idx (:idx state)
                                   :idx idx
                                   :first-transition-in? (not (:first-transition-in? state)))
                      ;; New display
                      (assoc-ins db
                        [root :idx] idx
                        [root :prev-idx] nil
                        [root :first-transition-in?] true)))))

(defn- nav-page-state [db map-key root page-key ids-key name query]
  (let [=name   (fn [card]
                  (= name (to-param-str (:name card))))
        obj-map (db map-key)
        q-ids   (ids-from-csv-query query)
        [obj-ids idx] (if q-ids
                        (let [objs (map-indexed             ; Not select-keys; we need to keep the order.
                                     (fn [idx id]
                                       (assoc (obj-map id) :idx idx))
                                     q-ids)
                              idx  (->>
                                     objs
                                     (filter =name)
                                     first
                                     :idx)]
                          [(mapv :id objs) idx])
                        (let [obj (->>
                                    obj-map
                                    (filter (fn [[_ v]]
                                              (=name v)))
                                    first
                                    second)]
                          [[(:id obj)] 0]))]
    (cond
      (and q-ids (some nil? obj-ids)) (set-error db "Invalid id in list")
      (and q-ids (not idx)) (set-error db "Not found in list")
      (and (not q-ids) (= [nil] obj-ids)) (set-error db "Not found")
      :else (-> db
              (nav-page-state-set-idx root page-key idx)
              (assoc-ins
                [root ids-key] obj-ids
                [root :ids-query-str] (to-query-array obj-ids))))))

(defn- cards-state [db _ query]
  (let [ids (ids-from-csv-query query)]
    (-> db
      ;; Do not change if ids param is not set
      (update-in [:cards-page :base] #(or ids %))
      (update-in [:cards-page :card-selection :ids] #(or (not-empty (set ids)) %)))))

(defn- card-details-state [db {:keys [card-name]} query]
  (nav-page-state db :cards :card-details-page :card-details :card-ids card-name query))

(defn display-setup-state [db {:keys [setup-name]} query]
  (nav-page-state db :setups :display-setup-page :display-setup :setup-ids setup-name query))

(defn route->state [db name params query]
  (let [state-fn (case name
                   :cards cards-state
                   :card-details card-details-state
                   :display-setup display-setup-state
                   (fn [db _ _] db))]
    (if (= :ready (:mode db))
      (-> db
        (assoc :current-page name)
        (back-button-state query)
        (state-fn params query))
      ;; Postpone state resolving. :load-data-success will dispatch :route.
      (assoc db :init-route [name params query]))))
