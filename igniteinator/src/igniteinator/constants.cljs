(ns igniteinator.constants)

(defonce page-url "https://igniteinator.waldeinburg.dk")
(defonce data-file-path "/generated/data.json")
(defonce img-base-path "/generated/img")
(defonce img-ext ".png")
(defonce default-language :en)

(defonce card-sizes {
                     :small  {:xs 4, :sm 3, :md 2, :lg 1}
                     :normal {:xs 6, :sm 4, :md 3, :lg 2}
                     :large  {:xs 12, :sm 6, :md 4, :lg 3}
                     :xlarge {:xs 12, :md 6}
                     })
