(ns igniteinator.model.card-sort
  [:require [igniteinator.util.sort :refer [keyfn-comparator]]])

(defn- cost-comparator [x y]
  (let [cx  (:cost x)
        cy  (:cost y)
        nx? (number? cx)
        ny? (number? cy)]
    ;; Normal cards precedes title cards.
    ;; Title cards have array of costs.
    (cond
      (and nx? (not ny?)) -1
      (and ny? (not nx?)) 1
      :else (compare cx cy))))

(defonce sortings
  {
   :name   (keyfn-comparator :name)
   :cost   cost-comparator
   :combos (fn [x y]
             (compare (count (:combos x)) (count (:combos y))))
   })
