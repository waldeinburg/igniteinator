(ns igniteinator.subs
  (:require-macros [reagent.ratom :as ra])
  (:require [clojure.string :as s]
            [igniteinator.constants :as constants]
            [igniteinator.model.cards :as cards-util]
            [igniteinator.model.epic-setups :refer [epic-setups]]
            [igniteinator.model.setups :as setups]
            [igniteinator.randomizer.card-specs :as randomizer.card-specs]
            [igniteinator.router :refer [resolve-to-href]]
            [igniteinator.subs-calc :refer [default-order-sortings]]
            [igniteinator.text :as text]
            [igniteinator.util.filter :refer [find-id-by-name-fn]]
            [igniteinator.util.filter :as filter-util]
            [igniteinator.util.image-path :refer [image-path]]
            [igniteinator.util.re-frame :refer [<sub <sub-ref reg-sub-db reg-sub-option]]
            [igniteinator.util.sort :as sort-util]
            [igniteinator.util.url :as url]
            [re-frame.core :refer [reg-sub reg-sub-raw]]))

(reg-sub-db :debug/show-card-data)

(reg-sub-db :language)
(reg-sub-db :mode)
(reg-sub-db :waiting?)
(reg-sub-db :fatal-message)
(reg-sub-db :current-page)

(reg-sub
  :translating?
  :<- [:language]
  (fn [lang _]
    (not= constants/default-language lang)))

(reg-sub
  :display-name?
  :<- [:display-name?-setting]
  :<- [:translating?]
  (fn [[setting translating?] _]
    (or (= :always setting) (and (= :translating setting) translating?))))

(reg-sub-db :back-page)

(reg-sub-db :main-menu-mobile/open?)
(reg-sub-db :language-menu-open?)
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
    (text/txt-lang lang key)))

(reg-sub
  :txt-c
  (fn [[_ key]]
    (<sub-ref :txt key))
  (fn [str _]
    (s/capitalize str)))

(reg-sub-db :boxes-setting [:options :boxes])
(reg-sub-option :size)
(reg-sub-option :default-order)
;; Use :display-name? for the actual state.
(reg-sub-db :display-name?-setting [:options :display-name?])

(reg-sub
  :boxes-setting/box?
  :<- [:boxes-setting]
  (fn [setting [_ box]]
    (setting (:id box))))
(reg-sub
  :boxes-setting/box-ks?
  (fn [[_ box]]
    (<sub-ref :boxes-setting/box? box))
  (fn [box?]
    (= :ks box?)))

(reg-sub
  :global-filters
  :<- [:boxes-setting]
  (fn [boxes-setting _]
    [{:key :box-and-ks, :args [boxes-setting]}]))

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
  :default-order-sortings
  :<- [:default-order]
  (fn [order _]
    (default-order-sortings order)))

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
  :<- [:global-filters]
  :<- [:select-cards-dialog/search-str]
  (fn [[global-filters search-str] _]
    (let [search-filter (if (not-empty search-str)
                          [{:key :name-contains, :args [search-str]}])
          filters       (into global-filters search-filter)
          sortings      [{:key :name, :order :asc}]]
      (<sub :cards :all filters sortings))))

(reg-sub
  :global-cards-unsorted
  :<- [:global-filters]
  (fn [global-filters _]
    (<sub :cards :all global-filters [])))

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
    (cards-map id)))

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
            preds (cards-util/filter-specs->preds filter-specs)
            comps (cards-util/sorting-specs->comparators sorting-specs)]
        (->>
          base
          (filter-util/filter-multi preds)
          (sort-util/sort-by-hierarchy comps))))))

(reg-sub
  :card/types-str
  (fn [db [_ card]]
    (s/join ", "
      (map #(-> db :types (get %) :name)
        (:types card)))))
(reg-sub
  :card/box-name
  (fn [db [_ card]]
    (-> db :boxes (get (:box card)) :name)))
(reg-sub
  :card/ks-exclusive?
  (fn [_ [_ card]]
    (true? (:ks card))))

(reg-sub-db
  :boxes-map
  [:boxes])
(reg-sub
  :all-boxes
  :<- [:boxes-map]
  (fn [boxes-map []]
    (sort-by :id (vals boxes-map))))
(reg-sub
  :boxes-by-ids
  :<- [:boxes-map]
  (fn [boxes-map [_ ids]]
    (vals (select-keys boxes-map ids))))

(reg-sub
  :cards-page/cards
  :<- [:cards-page/base]
  :<- [:cards-page.combos/value]
  :<- [:global-filters]
  :<- [:cards-page/filters]
  :<- [:cards-page/search-str]
  :<- [:default-order-sortings]
  :<- [:cards-page/sortings]
  (fn [[base combos-value global-filters page-filters search-str default-sortings page-sortings] _]
    (let [combos-base-filter (if (and (= :combos base) (= :all combos-value))
                               [{:key :has-combos}])
          search-filter      (if (not-empty search-str)
                               [{:key :name-contains, :args [search-str]}])
          sortings           (or (not-empty page-sortings) default-sortings)
          base-spec          (if combos-base-filter :all base)
          filters            (->> search-filter
                               (into combos-base-filter) (into page-filters) (into global-filters))]
      (<sub :cards base-spec filters sortings))))

(reg-sub
  :card-href
  (fn [_ [_ card]]
    (resolve-to-href :card-details {:card-name (url/to-param-str (:name card))})))

(reg-sub-db :card-details-page/idx)
(reg-sub-db :card-details-page/prev-idx)
(reg-sub-db :card-details-page/first-transition-in?)
(reg-sub-db :card-details-page/card-ids)
(reg-sub-db :card-details-page/initial-idx)
(reg-sub-db :card-details-page/sortings)

(let [reg-card-details-page-card-name
      (fn [name idx-sub]
        (reg-sub
          name
          :<- [:cards-map]
          :<- [:card-details-page/card-ids]
          :<- [idx-sub]
          (fn [[cards-map card-ids idx] _]
            (-> card-ids (get idx) cards-map :name))))]
  (reg-card-details-page-card-name :card-details-page/current-card-name :card-details-page/idx)
  (reg-card-details-page-card-name :card-details-page/previous-card-name :card-details-page/prev-idx))
(reg-sub
  :card-details-page/combos
  :<- [:default-order-sortings]
  :<- [:card-details-page/sortings]
  (fn [[default-sortings page-sortings] [_ card]]
    (<sub :cards (:combos card) [] (or page-sortings default-sortings))))

(reg-sub-db
  :setups-map
  [:setups])
(reg-sub
  :setups
  :<- [:setups-map]
  (fn [setups-map _]
    (vals setups-map)))
(reg-sub
  :setups-by-ids
  :<- [:setups-map]
  (fn [setups-map [_ ids]]
    (vals (select-keys setups-map ids))))
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

(reg-sub
  :boxes-with-setups
  :<- [:all-boxes]
  :<- [:setups]
  (fn [[boxes setups] _]
    (setups/filter-boxes-with-setups boxes setups)))

(reg-sub-db :setups-filter/operator)
(reg-sub-db :setups-filter/selection)
(reg-sub
  :setups-filter/box-selected?
  :<- [:setups-filter/selection]
  (fn [selection [_ id]]
    (contains? selection id)))
(reg-sub
  :setups-filtered-and-sorted
  :<- [:setups-sorted]
  :<- [:setups-filter/operator]
  :<- [:setups-filter/selection]
  (fn [[setups operator selection] _]
    (filterv
      (case operator
        :all #(= (set (:requires %)) selection)
        :some #(every? selection (set (:requires %))))
      setups)))
(reg-sub
  :setups-includes-recommended-starter-set?
  :<- [:setups-filtered-and-sorted]
  (fn [setups _]
    (= (-> setups first :id) 0)))

(reg-sub
  :setup-href
  (fn [_ [_ setup]]
    (resolve-to-href :display-setup {:setup-name (url/to-param-str (:name setup))})))

(reg-sub
  :setups-page-ids
  :<- [:setups-filtered-and-sorted]
  (fn [setups _]
    (map :id setups)))

(reg-sub-db :display-setup-page/idx)
(reg-sub-db :display-setup-page/setup-ids)
(reg-sub-db :display-setup-page/prev-idx)
(reg-sub-db :display-setup-page/first-transition-in?)
(reg-sub-db :display-setup-page/sortings)
(reg-sub
  :setup/cards
  :<- [:setups-map]
  :<- [:default-order-sortings]
  :<- [:display-setup-page/sortings]
  (fn [[setups default-order-sortings page-sortings] [_ id]]
    (let [setup (setups id)]
      (<sub :cards (:cards setup) [] (or page-sortings default-order-sortings)))))

(reg-sub
  :display-setup-page/setups
  :<- [:display-setup-page/setup-ids]
  (fn [setup-ids _]
    (<sub :setups-by-ids setup-ids)))
(let [reg-setup-page-name
      (fn [name idx-sub]
        (reg-sub
          name
          :<- [:setups-map]
          :<- [:display-setup-page/setup-ids]
          :<- [idx-sub]
          (fn [[setups-map setup-ids idx] _]
            (-> setup-ids (get idx) setups-map :name))))]
  (reg-setup-page-name :display-setup-page/current-setup-name :display-setup-page/idx)
  (reg-setup-page-name :display-setup-page/previous-setup-name :display-setup-page/prev-idx))

(reg-sub
  :setup/required-boxes
  :<- [:setups-map]
  (fn [setups [_ id]]
    (let [setup (setups id)]
      (<sub :boxes-by-ids (:requires setup)))))
(reg-sub
  :setup/required-boxes-string
  (fn [_ [_ id]]
    (let [boxes (<sub :setup/required-boxes id)]
      (s/join ", " (map :name boxes)))))

(reg-sub-db :share/dialog-open?)
(reg-sub-db :share/snackbar-open?)
(reg-sub-db :share/mode)

(reg-sub
  :share/cards
  :<- [:current-page]
  :<- [:cards-page/cards]
  (fn [[current-page cards-page-cards] _]
    (not-empty (case current-page
                 :cards cards-page-cards
                 nil))))
(reg-sub
  :share/url
  :<- [:share/cards]
  (fn [cards _]
    (if cards
      (str constants/page-url "?ids=" (s/join "-" (sort (map :id cards)))))))
(reg-sub
  :share/names
  :<- [:share/cards]
  (fn [cards _]
    (if cards
      (s/join ", " (map :name cards)))))
(reg-sub
  :share/value
  :<- [:share/mode]
  :<- [:share/url]
  :<- [:share/names]
  (fn [[mode url names] _]
    (case mode
      :url url
      :names names)))
(reg-sub
  :share/snackbar-text-formatted
  :<- [:share/mode]
  :<- [:txt :link]
  :<- [:txt :names]
  (fn [[mode link names] _]
    (text/txt :share/snackbar-text
      {:value (s/capitalize (case mode
                              :url link
                              :names names))})))

(reg-sub-db :install-dialog/open?)

(reg-sub-db :clear-data/dialog-open?)

(reg-sub-db :settings-menu-open?)

(reg-sub-db
  :types-map
  [:types])
(reg-sub
  :all-types
  :<- [:types-map]
  (fn [types-map _]
    (vals types-map)))

;; A set of constants and functions that are not available until data are loaded.
(reg-sub
  :filter-utils
  :<- [:all-cards]
  :<- [:all-types]
  :<- [:all-effects]
  (fn [[cards types effects] _]
    (let [find-card-id-by-name   (find-id-by-name-fn cards)
          find-type-id-by-name   (find-id-by-name-fn types)
          find-effect-id-by-name (find-id-by-name-fn effects)
          is-type-fn             (fn [type-name]
                                   (let [type-id (find-type-id-by-name type-name)]
                                     (fn [card]
                                       (= type-id (-> card :types (get 0))))))
          has-type-fn            (fn [type-name]
                                   (let [type-id (find-type-id-by-name type-name)]
                                     (fn [card]
                                       (some #(= type-id %) (:types card)))))
          provides-effect-fn     (fn [effect-name]
                                   (let [effect-id (find-effect-id-by-name effect-name)]
                                     (fn [card]
                                       (some #(= effect-id %) (:provides-effect card)))))]
      {:march-id             (find-card-id-by-name "March")
       :dagger-id            (find-card-id-by-name "Dagger")
       :old-wooden-shield-id (find-card-id-by-name "Old Wooden Shield")
       :dragon-potion-id     (find-card-id-by-name "Dragon Potion")
       :bow?                 (has-type-fn "Bow")
       :movement?            (is-type-fn "Movement")
       :ability?             (is-type-fn "Ability")
       :event?               (is-type-fn "Event")
       :weapon?              (is-type-fn "Weapon")
       :shield?              (is-type-fn "Shield")
       :war-machine?         (is-type-fn "War Machine")
       :projectile?          (is-type-fn "Projectile")
       :spell?               (is-type-fn "Spell")
       :item?                (is-type-fn "Item")
       :title?               (is-type-fn "Title")
       :provides-damage?     (provides-effect-fn "Damage")})))

(reg-sub-db :epic/show-stack-info?)
(reg-sub-db :epic/reset-dialog-open?)
(reg-sub-db :epic/active?)
(reg-sub-db :epic/stacks)
(reg-sub-db :epic/setup-idx)
(reg-sub-db :epic/trash-mode)

(reg-sub :epic/setups
  :<- [:filter-utils]
  :<- [:boxes-setting/box? {:id 2}]
  :<- [:boxes-setting/box? {:id 3}]
  (fn [args _]
    (apply epic-setups args)))


(reg-sub
  :epic/setup
  :<- [:epic/setups]
  :<- [:epic/setup-idx]
  (fn [[setups setup-idx]]
    (if setup-idx
      (setups setup-idx))))

(reg-sub
  :epic/trash-to-bottom?
  :<- [:epic/setup]
  (fn [setup _]
    (:trash-to-bottom? setup)))

(reg-sub
  :epic/top-cards
  :<- [:cards-map]
  :<- [:epic/stacks]
  (fn [[cards-map stacks] _]
    (let [empty-stack-card {:name       "Empty stack"
                            :image-path (str constants/img-base-path "/empty-stack.png")}
          top-cards        (map-indexed
                             (fn [idx stack]
                               (let [top-card (if-let [card-ids (not-empty (:cards stack))]
                                                (get cards-map (first card-ids))
                                                empty-stack-card)]
                                 ;; Add stack info
                                 (assoc top-card :stack (assoc stack :idx idx))))
                             stacks)]
      ;; Add index if the stack can be displayed.
      (loop [cards top-cards
             res   []
             idx   0]
        (if (empty? cards)
          res
          (let [[card & remaining-cards] cards
                stack (:stack card)
                [new-card new-idx] (if (or
                                         (:placeholder? stack)
                                         (= 0 (count (:cards stack))))
                                     [card idx]
                                     [(assoc card :nav-stack-idx idx) (inc idx)])]

            (recur remaining-cards (conj res new-card) new-idx)))))))

(reg-sub
  :epic/relevant-top-cards
  :<- [:epic/top-cards]
  (fn [top-cards _]
    (vec (filter :nav-stack-idx top-cards))))

(reg-sub-db :epic/cards-taken)

(reg-sub
  :epic/trash-button-disabled?
  :<- [:epic/cards-taken]
  (fn [cards-taken _]
    (empty? cards-taken)))

(reg-sub-db :epic/trash-dialog-open?)
(reg-sub-db :epic/trash-search-str)

(reg-sub
  :epic/trash-dialog-cards
  :<- [:epic/cards-taken]
  :<- [:epic/trash-search-str]
  (fn [[cards-taken search-str] _]
    (let [card-ids (keys cards-taken)
          filters  (if (not-empty search-str)
                     [{:key :name-contains, :args [search-str]}]
                     [])]
      (<sub :cards card-ids filters [{:key :name, :order :asc}]))))

;; Just the same as the dialog, though we might want to allow changing the order.
(reg-sub
  :epic/trash-page-cards
  :<- [:epic/trash-dialog-cards]
  (fn [cards _]
    cards))

(reg-sub-db :epic/snackbar-1-message)
(reg-sub-db :epic/snackbar-2-message)
(reg-sub-db :epic/snackbar-1-open?)
(reg-sub-db :epic/snackbar-2-open?)

(reg-sub-db :epic-display-stack-page/idx)
(reg-sub-db :epic-display-stack-page/prev-idx)
(reg-sub-db :epic-display-stack-page/first-transition-in?)
(let [reg-stack-page-title-sub
      (fn [name idx-sub]
        (reg-sub
          name
          :<- [:epic/relevant-top-cards]
          :<- [idx-sub]
          (fn [[relevant-top-cards idx] _]
            (-> relevant-top-cards (get idx) :name))))]
  (reg-stack-page-title-sub :epic-display-stack-page/current-title :epic-display-stack-page/idx)
  (reg-stack-page-title-sub :epic-display-stack-page/previous-title :epic-display-stack-page/prev-idx))

(let [reg-history-title-sub
      (fn [name history-key]
        (reg-sub
          name
          (fn [{:keys [cards epic]} _]
            (if-let [history (epic history-key)]
              (let [stacks      (:stacks epic)
                    {:keys [action card-id stack-idx]} (first history)
                    verb        (case action
                                  :take "take"
                                  :cycle "cycle"
                                  :trash "trash")
                    preposition (case action
                                  :take "from"
                                  :cycle "in"
                                  :trash "to")]
                (str (s/capitalize verb) " " (-> card-id cards :name) " "
                  preposition " " (-> stack-idx stacks :name)))))))]
  (reg-history-title-sub :epic/undo-title :undo-history)
  (reg-history-title-sub :epic/redo-title :redo-history))

(reg-sub-db :effects-map [:effects])
(reg-sub
  :all-effects
  :<- [:effects-map]
  (fn [effects-map _]
    (vals effects-map)))

(reg-sub
  :randomizer/all-cards
  :<- [:cards :all [{:key :not-starter}] [{:key :name, :order :asc}]]
  (fn [cards _]
    cards))

(reg-sub-db :randomizer/cards-base)
(reg-sub-db :randomizer/selected-cards)
(reg-sub-db :randomizer/replace-from)
(reg-sub-db :randomizer/edit?)
(reg-sub-db :randomizer/changed?)
(reg-sub-db :randomizer/replace-using-specs?)
(reg-sub-db :randomizer/show-specs?)

(reg-sub
  :randomizer/market
  :<- [:randomizer/selected-cards]
  :<- [:randomizer/show-specs?]
  :<- [:randomizer/edit?]
  :<- [:default-order-sortings]
  (fn [[selected-cards show-specs? edit? default-sortings] _]
    (if show-specs?
      selected-cards
      (if edit?
        (sort-by :order-idx selected-cards)
        (sort-util/sort-by-hierarchy (cards-util/sorting-specs->comparators default-sortings) selected-cards)))))

(reg-sub
  :randomizer/market-generated?
  :<- [:randomizer/selected-cards]
  (fn [selected-cards _]
    (boolean selected-cards)))

(reg-sub
  :randomizer/specs
  :<- [:filter-utils]
  (fn [filter-utils]
    (randomizer.card-specs/card-specs filter-utils)))

(reg-sub
  :randomizer/card-ids-to-shuffle
  :<- [:randomizer/cards-base]
  :<- [:global-filters]
  :<- [:cards-page/cards]
  (fn [[cards-base global-filters card-page-cards] _]
    (mapv :id (case cards-base
                :all (<sub :cards :all global-filters [])
                :cards-page card-page-cards))))

(reg-sub
  :randomizer/some-unresolved
  :<- [:randomizer/selected-cards]
  (fn [selected-cards _]
    (some :unresolved? selected-cards)))

(reg-sub
  :randomizer/sort-button-disabled?
  :<- [:randomizer/show-specs?]
  :<- [:randomizer/changed?]
  (fn [[show-specs? changed?] _]
    (or show-specs? (not changed?))))
