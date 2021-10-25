(ns igniteinator.model.setups)

(defn filter-boxes-with-setups [boxes setups]
  (let [setups-pred (fn [f box]
                      (let [id (:id box)]
                        (f #(some #{id} (:requires %))
                          setups)))]
    (->>
      boxes
      (filter #(setups-pred some %))
      (map #(assoc % :required-by-all? (setups-pred every? %))))))
