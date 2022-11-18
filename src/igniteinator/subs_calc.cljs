(ns igniteinator.subs-calc)

(defn default-order-sortings [default-order]
  (case default-order
    :cost-name [{:key :cost, :order :asc}
                {:key :name, :order :asc}]
    :name [{:key :name, :order :asc}]))
