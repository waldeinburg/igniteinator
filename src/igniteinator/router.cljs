(ns igniteinator.router
  (:require [bide.core :as r]
            [clojure.string :as s]
            [igniteinator.util.re-frame :refer [>evt assoc-ins]]))

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
  (let [ids   (ids-from-csv-query query)
        cards (vals (select-keys (:cards db) ids))]
    ;; TODO: den skal kunne returnere en error-state. Måske bare en sæt current page = error (som skal have et hex-kort)
    (nav-page-state db :card-details-page :card-details 0)))

(defn route->state [db name params query]
  (let [state-fn (case name
                   :cards cards-state
                   :card-details card-details-state
                   (fn [db _ _] db))]
    (-> db
      (assoc :current-page name)
      (state-fn params query))))
