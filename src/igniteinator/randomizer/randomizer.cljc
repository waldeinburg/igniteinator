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

(defn generate-market-base [shuffled-cards specs]
  "Generate base for market based on specs. The cards argument should be a shuffled collection."
  (reduce
    (fn [[selected-cards cards-left] spec]
      (let [f                  ((:filter spec) selected-cards)
            card               (or
                                 (first (filter f cards-left))
                                 ;; If the filter returns none (e.g., no combo when selecting combos).
                                 (first cards-left))
            card-id            (:id card)
            new-cards-left     (filter #(not= card-id (:id %)) cards-left)
            new-selected-cards (conj selected-cards card)]
        [new-selected-cards new-cards-left]))
    [[] shuffled-cards]
    specs))

(defn has-requirements? [card]
  (some #{:requires-effect :requires-type} (keys card)))

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

(defn set-requirement-pred [selected-cards]
  (map
    #(assoc % :requirement-pred (get-full-requirement-pred %))
    selected-cards))

(defn set-depends-on [market-base-with-requirement-pred]
  "Add set :depends-on-idx on each card in market with all indexes in market that satisfy requirements."
  (let [market-vec (vec market-base-with-requirement-pred)]
    (map
      (fn [card-to-resolve]
        (if-let [pred (:requirement-pred card-to-resolve)]
          (let [depends-on-idx (reduce-kv
                                 (fn [res idx card]
                                   (if (pred card)
                                     (conj res idx)))
                                 #{}
                                 market-vec)]
            (assoc card-to-resolve :depends-on-idx depends-on-idx))
          card-to-resolve))
      market-base-with-requirement-pred)))

(defn set-satisfies [market-with-depends-on]
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

(defn unresolved? [market-with-requirement-pred card]
  (let [pred (:requirement-pred card)]
    (and pred
      ;; No cards matches predicate.
      (empty? (filter pred market-with-requirement-pred)))))

(defn set-spec-idx [market]
  (map-indexed (fn [idx card]
                 (assoc card :spec-idx idx))
    market))

(defn set-spec-name [market-with-spec-idx specs]
  (map (fn [card]
         (assoc card :spec-name
                     (let [idx (:spec-idx card)]
                       (if (< idx (count specs))
                         (:name (nth specs idx))
                         "Title card"))))
    market-with-spec-idx))

(defn set-unresolved? [market-with-requirement-pred]
  (map
    (fn [card]
      (assoc card :unresolved?
                  (unresolved? market-with-requirement-pred card)))
    market-with-requirement-pred))

(defn mark-dependencies [market-base]
  (->
    market-base
    set-requirement-pred
    set-depends-on
    set-satisfies
    vec))

(defn add-final-metadata [market-with-requirement-pred specs]
  (->
    market-with-requirement-pred
    set-spec-idx
    (set-spec-name specs)
    set-unresolved?
    vec))

(defn replace-card [new-card-pred use-specs? specs selected-cards cards-left idx-to-replace]
  (let [card-to-replace (nth selected-cards idx-to-replace)
        valid-card-pred (if use-specs?
                          (let [spec-filter (:filter (nth specs idx-to-replace))]
                            (spec-filter (assoc selected-cards idx-to-replace nil)))
                          any?)
        valid-cards     (filter valid-card-pred cards-left)]
    (if-let [new-card (or
                        (first (filter new-card-pred valid-cards))
                        ;; If there's for some reason no cards satisfying both the spec and the requirement, we must
                        ;; satisfy the requirement from the full deck. We can safely assume that this will always be
                        ;; possible (the contrary would be extremely inflexible cards or idx-to-replace reaching the
                        ;; movement card filters which is not going to happen).
                        (first (filter new-card-pred cards-left)))]
      (let [new-card-id             (:id new-card)
            new-cards-left          (conj
                                      ;; Remove new card.
                                      (filterv #(not= new-card-id (:id %)) cards-left)
                                      ;; Insert replaced card to "bottom" of the random deck.
                                      card-to-replace)
            selected-cards-replaced (assoc selected-cards idx-to-replace new-card)
            ;; Always recalculate. The card could satisfy another dependency already satisfied by one other card,
            ;; meaning that another card should now lose its :requirement? tag.
            ;; There's other cases requiring a recalculation, but we could test for those:
            ;; 1. (:satisfies-some? card-to-replace): The card was not the only card that satisfied some requirement (it
            ;; would have been skipped) but it satisfied some requirement. This mean that there might be another card
            ;; which is not the only card that satisfied that requirement.
            ;; 2. (has-requirements? new-card): The new card has requirements. We will either run into this card if
            ;; idx-to-resolve < idx-to-replace or we catch the unresolved dependency when we check if we can safely
            ;; return from the loop.
            new-selected-cards      (mark-dependencies selected-cards-replaced)]
        [new-selected-cards new-cards-left]))))

(defn resolve-requirements [shuffled-cards market-base specs]
  (let [last-idx (dec (count market-base))]
    (loop [cards-left     shuffled-cards
           selected-cards (mark-dependencies market-base)
           idx-to-resolve 0
           idx-to-replace last-idx
           replaced-any?  false
           ;; Replace cards having requirements? Our first priority is not to because requirements are a sort of combo.
           ;; Should this be a preserve-combos? No, it would require that we included check that the card rule is not a
           ;; combo selection, or else we would have a risk of skipping all combo rules and resolving by replacing the
           ;; cost 10-11 card, which would either fail or be deterministic for arrow/bow (Deady Arrow and Modified Bow
           ;; are from the expansion), the requirement type most likely to be resolved.
           preserve-reqs? true]
      (if (or (> idx-to-resolve last-idx) (< idx-to-replace 0))
        (if (and
              ;; If we haven't replaced any in the last run and the false preserve-reqs fallback has failed, then
              ;; terminate to avoid infinite loop (the next try will also fail).
              (or replaced-any? preserve-reqs?)
              ;; Any unresolved after running through all cards?
              (some (partial unresolved? selected-cards) selected-cards))
          ;; We need another run to resolve all cards. This can happen if idx-to-resolve runs past idx-to-replace and
          ;; a card with an unfulfilled requirements is selected. Example: Arrow Storm is selected as the last card in
          ;; a market without any Projectile or Bow cards or any other card with requirements. When idx-to-resolve is
          ;; 15 then idx-to-replace goes down to 14 to avoid replacing idx 15, a Projectile card replaces idx 14, and
          ;; we now have run through the whole market but is ending up with a Projectile missing a Bow.
          (recur cards-left selected-cards 0 last-idx false
            ;; If we did not replace any cards in the last run and still have unresolved dependencies, it means that our
            ;; strategy of avoiding replacing cards with requirements did not work out (will happen if all cards have
            ;; requirements but one of them is not satisfied).
            replaced-any?)
          [selected-cards cards-left])
        (let [card-to-resolve (nth selected-cards idx-to-resolve)
              card-to-replace (nth selected-cards idx-to-replace)]
          (if (or
                ;; This card is the only card that satisfies some requirement.
                (:requirement? card-to-replace)
                ;; We cannot resolve a card by replacing the card itself. We could select a card without requirements,
                ;; but we will not do that because requirements are a sort of combo; and we want to prioritize cards
                ;; that fit together. Of course, this only applies if the card actually has any requirements. If not,
                ;; we can proceed, the resolving will do nothing and the card may be replaced when resolving the next.
                (and (= idx-to-replace idx-to-resolve) (:requirement-pred card-to-resolve))
                ;; For the same reason mentioned above, do not replace the card if it has requirements unless that
                ;; priority has failed. The preserve-reqs? condition does not apply to replacing the same card for
                ;; simplicity.
                (and preserve-reqs? (:requirement-pred card-to-replace)))
            ;; Then do not replace this card.
            (recur cards-left selected-cards idx-to-resolve (dec idx-to-replace) replaced-any? preserve-reqs?)
            ;; Check requirements and resolve if any.
            (let [new-idx-to-resolve (inc idx-to-resolve)]
              ;; Do not calculate :unresolved? beforehand. The card could have been resolved when another card was
              ;; resolved.
              (if (unresolved? selected-cards card-to-resolve)
                (let [new-card-pred (:requirement-pred card-to-resolve)]
                  (if-let [[new-selected-cards new-cards-left]
                           (replace-card new-card-pred true specs selected-cards cards-left idx-to-replace)]
                    (recur new-cards-left new-selected-cards new-idx-to-resolve
                      (dec idx-to-replace) true preserve-reqs?)
                    ;; We failed to find a card that satisfied the dependency, even outside the spec. Replace the card
                    ;; to resolve. If the new card has requirements we will handle those in the next loop.
                    (let [[new-selected-cards new-cards-left]
                          (replace-card any? true specs selected-cards cards-left idx-to-resolve)]
                      (recur new-cards-left new-selected-cards new-idx-to-resolve idx-to-replace true preserve-reqs?))))
                ;; Nothing to do for this card.
                (recur cards-left selected-cards new-idx-to-resolve idx-to-replace replaced-any? preserve-reqs?)))))))))

(defn add-title-cards [selected-cards random-title-cards]
  (let [[a b & title-cards-left] random-title-cards
        full-market (into (vec selected-cards) [a b])]
    [full-market title-cards-left]))

(defn replace-title-card [selected-cards title-cards-left idx-to-replace]
  (let [new-card             (first title-cards-left)
        card-to-replace      (nth selected-cards idx-to-replace)
        new-selected-cards   (assoc selected-cards idx-to-replace new-card)
        new-title-cards-left (conj (vec (rest title-cards-left)) card-to-replace)]
    [new-selected-cards new-title-cards-left]))

(defn replace-selected-card [{:keys [title?]} use-specs? specs
                             selected-cards cards-left
                             title-cards-left idx-to-replace]
  (let [card-to-replace (nth selected-cards idx-to-replace)
        ;; If editing while showing sorted market, preserve new card in place.
        order-idx       (:order-idx card-to-replace)
        title-card?     (title? card-to-replace)]
    (let [[new-selected-cards new-cards-left]
          (if title-card?
            (replace-title-card selected-cards title-cards-left idx-to-replace)
            (let [card-to-replace (nth selected-cards idx-to-replace)
                  ;; If the card is unresolved, replace it by any card. If there's any other unresolved card, replace
                  ;; by resolving.
                  new-card-pred   (if (:unresolved? card-to-replace)
                                    any?
                                    (if-let [first-unresolved-card (first (filter :unresolved? selected-cards))]
                                      (:requirement-pred first-unresolved-card)
                                      any?))]
              (replace-card new-card-pred use-specs? specs selected-cards cards-left idx-to-replace)))
          ;; Update dependency data on market.
          final-market (->
                         new-selected-cards
                         mark-dependencies
                         (add-final-metadata specs)
                         (update idx-to-replace #(assoc % :order-idx order-idx)))]
      (if title-card?
        [final-market cards-left new-cards-left]
        [final-market new-cards-left title-cards-left]))))

(defn generate-market [filter-utils shuffled-cards-all specs]
  (let [shuffled-cards       (get-randomizer-cards filter-utils shuffled-cards-all)
        shuffled-title-cards (get-title-cards filter-utils shuffled-cards-all)
        [market-base cards-left] (generate-market-base shuffled-cards specs)
        [resolved-market final-cards-left] (resolve-requirements cards-left market-base specs)
        [full-market title-cards-left] (add-title-cards resolved-market shuffled-title-cards)
        final-market         (add-final-metadata full-market specs)]
    [final-market final-cards-left title-cards-left]))
