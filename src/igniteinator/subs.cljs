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
(reg-sub-db :fatal-message)
(reg-sub-db :current-page)

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
(reg-sub-db :cards-page.card-selection/dialog-open?)
(reg-sub-db :cards-page.card-selection/selection
  [:cards-page :card-selection :ids])

(reg-sub-db :select-cards-dialog/search-str)
(reg-sub
  :select-cards-dialog/cards
  (fn [_ _]
    (ra/reaction
      (let [search-str (<sub :select-cards-dialog/search-str)
            filters    (if (empty? search-str)
                         []
                         [{:key :name-contains, :args [search-str]}])
            sortings   [{:key :name, :order :asc}]]
        (<sub :cards :all filters sortings)))))

(reg-sub
  :cards-page.card-selection/item-selected?
  :<- [:cards-page.card-selection/selection]
  (fn [selection [_ id]]
    (contains? selection id)))

(reg-sub-db
  :cards-map
  [:cards])

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
                    (<sub :cards-by-ids base-spec))
            preds (cards/filter-specs->preds filter-specs)
            comps (cards/sorting-specs->comparators sorting-specs)]
        (->>
          base
          (filter-util/filter-multi preds)
          (sort-util/sort-by-hierarchy comps))))))

(reg-sub-raw
  :cards-page/cards
  (fn [_ _]
    (ra/reaction
      (let [base         (<sub :cards-page/base)
            base-filters (<sub :cards-page/filters)
            s-str        (<sub :cards-page/search-str)
            sortings     (<sub :cards-page/sortings)
            filters      (if (empty? s-str)
                           base-filters
                           (conj base-filters {:key :name-contains, :args [s-str]}))]
        (<sub :cards base filters sortings)))))

(reg-sub-db :card-details-page/card-id)
(reg-sub-db :card-details-page/sortings)

(reg-sub-raw
  :card-details-page/card
  (fn [_ _]
    (ra/reaction
      (let [card-id (<sub :card-details-page/card-id)]
        (<sub :card card-id)))))

(reg-sub-db :install-dialog/open?)
