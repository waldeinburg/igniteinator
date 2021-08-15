(ns igniteinator.constants)

(def version "0.1.4")
(def page-url "https://igniteinator.waldeinburg.dk")
(def github-url "https://github.com/waldeinburg/igniteinator")
(def img-base-path "/img")
(def data-file-path "/generated/data.json")
(def gen-img-base-path "/generated/img")
(def gen-img-ext ".png")
(def default-language :en)
;; Link to a site that gives a quick overview of regexp. This one looks nice.
(def regular-expressions-site "https://cheatography.com/davechild/cheat-sheets/regular-expressions/")
;; This might change.
(def ignite-link "http://gingersnapgaming.com")

(def card-sizes {
                 :small  {:xs 4, :sm 3, :md 2, :lg 1}
                 :normal {:xs 6, :sm 4, :md 3, :lg 2}
                 :large  {:xs 12, :sm 6, :md 4, :lg 3}
                 :xlarge {:xs 12, :md 6}
                 })
