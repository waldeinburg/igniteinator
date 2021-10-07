(ns igniteinator.subs
  (:require [igniteinator.text :refer [txt]]
            [igniteinator.constants :as constants]
            [igniteinator.util.image-path :refer [image-path]]
            [igniteinator.util.re-frame :refer [reg-sub-db <sub]]
            [igniteinator.model.cards :as cards]
            [igniteinator.util.sort :as sort-util]
            [igniteinator.util.filter :as filter-util]
            [re-frame.core :refer [reg-sub reg-sub-raw subscribe]]
            [clojure.string :as s])
  (:require-macros [reagent.ratom :as ra]))

(reg-sub-db :language)
(reg-sub-db :mode)
(reg-sub-db :waiting?)
(reg-sub-db :fatal-message)
(reg-sub-db :current-page)

(reg-sub :page-history-not-empty?
  (fn [db _]
    (not-empty (:page-history db))))

(reg-sub-db :main-menu-mobile/open?)

(reg-sub-db :reload-snackbar/open?)

(reg-sub
  :caching-progress/initiated?
  (fn [db _]
    (-> db :caching-progress nil? not)))
(reg-sub-db :caching-progress/open?)
(reg-sub-db :caching-progress/count)
(reg-sub-db :caching-progress/progress)

(reg-sub
  :txt
  :<- [:language]
  (fn [lang [_ key]]
    (txt lang key)))

(reg-sub
  :txt-c
  (fn [[_ key]]
    (subscribe [:txt key]))
  (fn [str _]
    (s/capitalize str)))

(reg-sub-db :size)

(reg-sub
  :size+1
  :<- [:size]
  (fn [size _]
    (min
      (inc size)
      (dec (count constants/grid-breakpoints)))))

(reg-sub :grid-breakpoints
  :<- [:size]
  (fn [size _]
    (constants/grid-breakpoints size)))

(reg-sub
  :grid-breakpoints+1
  :<- [:size+1]
  (fn [size _]
    (constants/grid-breakpoints size)))

(reg-sub
  :image-path
  :<- [:language]
  (fn [lang [_ card]]
    (image-path lang card)))

(reg-sub
  :card-load-state
  (fn [db [_ card]]
    (let [lang (:language db)
          id   (:id card)]
      (get-in db [:card-load-state lang id]))))

(reg-sub-db :cards-page/search-str)
(reg-sub-db :cards-page/base)
(reg-sub-db :cards-page/filters)
(reg-sub-db :cards-page/sortings)
(reg-sub-db :cards-page.combos/dialog-open?)
(reg-sub-db :cards-page.combos/value)
(reg-sub-db :cards-page.card-selection/dialog-open?)
(reg-sub-db :cards-page.card-selection/selection
  [:cards-page :card-selection :ids])

(reg-sub-db :select-cards-dialog/search-str)
(reg-sub
  :select-cards-dialog/cards
  :<- [:select-cards-dialog/search-str]
  (fn [search-str _]
    (let [filters  (if (empty? search-str)
                     []
                     [{:key :name-contains, :args [search-str]}])
          sortings [{:key :name, :order :asc}]]
      (<sub :cards :all filters sortings))))

(reg-sub
  :cards-page.card-selection/item-selected?
  :<- [:cards-page.card-selection/selection]
  (fn [selection [_ id]]
    (contains? selection id)))

(reg-sub-db
  :cards-map
  [:cards])

(reg-sub-db :combos-set)
(reg-sub
  :combos-set-cards
  :<- [:combos-set]
  (fn [combos-set _]
    (<sub :cards-by-ids combos-set)))

(reg-sub
  :all-card-ids
  :<- [:cards-map]
  (fn [cards-map _]
    (keys cards-map)))

(reg-sub
  :all-cards
  :<- [:cards-map]
  (fn [cards-map _]
    (vals cards-map)))

(reg-sub
  :cards-by-ids
  :<- [:cards-map]
  (fn [cards-map [_ ids]]
    (vals (select-keys cards-map ids))))

(reg-sub
  :card
  :<- [:cards-map]
  (fn [cards-map [_ id]]
    (get cards-map id)))

;; Get cards based on filters and sortings from model.
;; Base is :all or a sequence of ids.
;; Arg filter-specs can be a single map {:key ..., args ...} or an sequence of those, with :key
;; referring into card-filter/filters. If a single map, then :key must refer to a function that in
;; itself returns the cards.
;; Arg sortings-specs must be a sequence of {:key ..., :order ...}, with :key referring into
;; card-sort/sortings and :order being :asc or :desc.
(reg-sub-raw
  :cards
  (fn [_ [_ base-spec filter-specs sorting-specs]]
    (ra/reaction
      (let [base  (case base-spec
                    :all (<sub :all-cards)
                    :combos (<sub :combos-set-cards)
                    (<sub :cards-by-ids base-spec))
            preds (cards/filter-specs->preds filter-specs)
            comps (cards/sorting-specs->comparators sorting-specs)]
        (->>
          base
          (filter-util/filter-multi preds)
          (sort-util/sort-by-hierarchy comps))))))

(reg-sub-db
  :boxes-map
  [:boxes])
(reg-sub
  :boxes-by-ids
  :<- [:boxes-map]
  (fn [boxes-map [_ ids]]
    (vals (select-keys boxes-map ids))))

(reg-sub
  :cards-page/cards
  :<- [:cards-page/base]
  :<- [:cards-page.combos/value]
  :<- [:cards-page/filters]
  :<- [:cards-page/search-str]
  :<- [:cards-page/sortings]
  (fn [[base combos-value page-filters search-str sortings] _]
    (let [combos-base-filter (if (and (= :combos base) (= :all combos-value))
                               [{:key :has-combos}])
          base-spec          (if combos-base-filter :all base)
          base-filters       (into page-filters combos-base-filter)
          filters            (if (empty? search-str)
                               base-filters
                               (conj base-filters {:key :name-contains, :args [search-str]}))]
      (<sub :cards base-spec filters sortings))))

(reg-sub-db :card-details-page/card-id)
(reg-sub-db :card-details-page/sortings)

(reg-sub
  :card-details-page/card
  :<- [:card-details-page/card-id]
  (fn [card-id _]
    (<sub :card card-id)))

(reg-sub-db
  :setups-map
  [:setups])
(reg-sub
  :setups
  :<- [:setups-map]
  (fn [setups-map _]
    (vals setups-map)))
(reg-sub
  :setups-sorted
  :<- [:setups]
  (fn [setups _]
    (sort (fn [x y]
            (cond
              ;; Special id 0, Recommened starter set, on top.
              (zero? (:id x)) -1
              (zero? (:id y)) 1
              :else (compare (:name x) (:name y))))
      setups)))

(reg-sub-db
  :display-setup-page/sortings)
(reg-sub-db
  :setup/id)
(reg-sub
  :current-setup
  :<- [:setups-map]
  :<- [:setup/id]
  (fn [[setups-map id] _]
    (get setups-map id)))
(reg-sub
  :current-setup/name
  :<- [:current-setup]
  (fn [setup _]
    (:name setup)))
(reg-sub
  :current-setup/cards
  :<- [:current-setup]
  :<- [:display-setup-page/sortings]
  (fn [[setup sortings] _]
    (<sub :cards (:cards setup) [] sortings)))
(reg-sub
  :current-setup/required-boxes
  :<- [:current-setup]
  (fn [setup _]
    (<sub :boxes-by-ids (:requires setup))))
(reg-sub
  :current-setup/required-boxes-string
  :<- [:current-setup/required-boxes]
  (fn [boxes _]
    (s/join ", " (map :name boxes))))
(reg-sub-db :install-dialog/open?)
