(ns igniteinator.util.media-query
  (:require [reagent-mui.material.use-media-query :refer [use-media-query]]
            [clojure.string :as str]))

(defn- min-max-media-query-str [breakpoints]
  (str/join ","
    (filter identity
      (map (fn [[min-width max-width]]
             (let [min-q (if min-width (str "(min-width:" min-width "px)"))
                   max-q (if max-width (str "(max-width:" max-width "px)"))]
               (cond
                 (and min-q max-q) (str min-q " and " max-q)
                 min-q min-q
                 max-q max-q)))
        breakpoints))))

(defn min-max-media-query [breakpoints]
  "Build media-query from list of [min-width max-width] pairs."
  (use-media-query (min-max-media-query-str breakpoints)))
