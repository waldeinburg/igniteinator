(ns igniteinator.randomizer.randomizer)

(defn get-randomizer-cards [{:keys [march-id dagger-id old-wooden-shield-id title?]} cards]
  "Get cards used for randomizer, i.e., exclude title cards and starter cards."
  (let [starters #{march-id dagger-id old-wooden-shield-id}]
    (filter
      (fn [card]
        (not (or
               (starters (:id card))
               (title? card))))
      cards)))

(defn get-title-cards [{:keys [title?]} cards]
  (filter title? cards))

(defn generate-market-base [random-cards specs]
  "Generate base for market based on specs. The cards argument should be a shuffled collection."
  (reduce
    (fn [[cards-left selected-cards] spec]
      (let [f                  ((:filter spec) selected-cards)
            card               (or
                                 (first (filter f cards-left))
                                 ;; If the filter returns none (e.g., no combo when selecting combos).
                                 (first cards-left))
            card-id            (:id card)
            new-cards-left     (filter #(not= card-id (:id %)) cards-left)
            new-selected-cards (conj selected-cards card)]
        [new-cards-left new-selected-cards]))
    [random-cards []]
    specs))

(defn get-requirement-pred [card-to-resolve key pred-fn]
  (if-let [req (get card-to-resolve key)]
    (let [req-set (set req)]
      (pred-fn req-set))))

(defn get-full-requirement-pred [card-to-resolve]
  "Get predicate checking all requirements of a card or nil if not any requirements."
  (if-let [preds (not-empty
                   (filter identity
                     (list
                       ;; Provides any of effects.
                       (get-requirement-pred card-to-resolve :requires-effect
                         (fn [req-set]
                           #(some req-set (:provides-effect %))))
                       ;; Has any of type.
                       (get-requirement-pred card-to-resolve :requires-type
                         (fn [req-set]
                           #(some req-set (:types %))))
                       ;; Does not have any of types.
                       (get-requirement-pred card-to-resolve :requires-type-except
                         (fn [req-set]
                           #(not-any? req-set (:types %)))))))]
    (let [card-id   (:id card-to-resolve)
          all-preds (cons
                      ;; Not the card itself (important for Counter Spell which requires Spell but is itself a spell).
                      #(not= card-id (:id %))
                      preds)]
      (apply every-pred all-preds))))

(defn mark-depends-on [market-base]
  "Add set :depends-on-idx on each card in market with all indexes in market that satisfy requirements."
  (map
    (fn [card-to-resolve]
      (if-let [pred (get-full-requirement-pred card-to-resolve)]
        (let [depends-on-idx (reduce-kv
                               (fn [res idx card]
                                 (if (pred card)
                                   (conj res idx)))
                               #{}
                               market-base)]
          (assoc card-to-resolve :depends-on-idx depends-on-idx))
        card-to-resolve))
    market-base))

(defn mark-satisfies [market-with-depends-on]
  "Mark that a card is the only card that satisfies some requirement."
  (map-indexed
    (fn [idx card-to-resolve]
      (let [requirement    (some (fn [card]
                                   (let [depends-on-idx (:depends-on-idx card)]
                                     (and
                                       (get depends-on-idx idx)
                                       (= 1 (count depends-on-idx)))))
                             market-with-depends-on)
            satisfies-some (some #(get (:depends-on-idx %) idx) market-with-depends-on)]
        (assoc card-to-resolve
          :requirement? (any? requirement)
          :satisfies-some? (any? satisfies-some))
        card-to-resolve))
    market-with-depends-on))

(defn mark-dependencies [market-base]
  (->
    market-base
    mark-depends-on
    mark-satisfies))

(defn resolve-requirements [random-cards market-base specs]
  (loop [cards-left     random-cards
         selected-cards (mark-dependencies market-base)
         idx-to-resolve 0
         idx-to-replace (dec (count selected-cards))]
    (if (= idx-to-resolve (count selected-cards))
      selected-cards
      (if (:requirement? (nth selected-cards idx-to-replace))
        ;; We need to skip this card as it is the only card that satisfies some requirement.
        (recur cards-left selected-cards idx-to-resolve (dec idx-to-replace))
        ;; Check requirements and resolve if any.
        (let [new-idx-to-resolve (inc idx-to-resolve)
              card-to-resolve    (nth selected-cards idx-to-resolve)
              new-card-pred      (if-let [pred (get-full-requirement-pred card-to-resolve)]
                                   ;; No cards matches predicate.
                                   (if (not-empty (filter pred selected-cards))
                                     pred))]
          (if new-card-pred
            (let [valid-card-pred         ((:filter (nth specs idx-to-replace)) selected-cards)
                  valid-cards             (filter valid-card-pred cards-left)
                  new-card                (if (<= idx-to-replace idx-to-resolve)
                                            (or
                                              (first (filter new-card-pred valid-cards))
                                              ;; If there's for some reason no cards satisfying both the spec and the
                                              ;; requirement, we must satisfy the requirement from the full deck.
                                              (first (filter new-card-pred cards-left)))
                                            ;; We cannot resolve the card because it's the card to be replaced, or we have
                                            ;; replaced cards past the cards to resolve. Select a card without
                                            ;; requirements.
                                            (first (filter (fn [card]
                                                             (not-any? #(contains? % card)
                                                               [:requires-effect
                                                                :requires-type
                                                                :requires-additional-of-type]))
                                                     valid-cards)))
                  card-to-replace         (nth selected-cards idx-to-replace)
                  new-card-id             (:id new-card)
                  new-cards-left          (conj
                                            ;; Remove new card.
                                            (filterv #(not= new-card-id (:id %)) cards-left)
                                            ;; Insert replaced card to "bottom" of the random deck.
                                            card-to-replace)
                  selected-cards-replaced (assoc selected-cards idx-to-replace new-card)
                  new-selected-cards      (if (:satisfies-some? card-to-replace)
                                            ;; The card was not the only card that satisfied some requirement (it would
                                            ;; have been skipped) but it satisfied some requirement. This mean that there
                                            ;; might be another card which is not the only card that satisfied that
                                            ;; requirement. Recalculate.
                                            (mark-dependencies selected-cards-replaced)
                                            selected-cards-replaced)]
              (recur new-cards-left new-selected-cards new-idx-to-resolve (dec idx-to-replace)))
            ;; Nothing to do for this card.
            (recur cards-left selected-cards new-idx-to-resolve idx-to-replace)))))))

(defn add-title-cards [selected-cards random-title-cards]
  (into (vec selected-cards) (take 2 random-title-cards)))

(defn generate-market [filter-utils random-cards-all specs]
  (let [random-cards       (get-randomizer-cards filter-utils random-cards-all)
        random-title-cards (get-title-cards filter-utils random-cards-all)
        [cards-left market-base] (generate-market-base random-cards specs)
        resolved-market    (resolve-requirements cards-left market-base specs)]
    (add-title-cards resolved-market random-title-cards)))
