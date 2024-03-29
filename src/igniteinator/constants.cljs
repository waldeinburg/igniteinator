(ns igniteinator.constants)

(def version "1.0.2")
(def page-url "https://igniteinator.waldeinburg.dk")
(def github-url "https://github.com/waldeinburg/igniteinator")
(def img-base-path "/img")
(def placeholder-img-src (str img-base-path "/placeholder.png"))
(def data-file-path "/data.json")
(def gen-img-base-path "/generated/img")
(def gen-img-ext ".png")
(def default-language :en)
;; Link to a site that gives a quick overview of regexp. This one looks nice.
(def regular-expressions-site "https://cheatography.com/davechild/cheat-sheets/regular-expressions/")
;; This might change.
(def ignite-link "https://gingersnapgaming.com")

(def languages [{:id   :en
                 :name "English"}
                {:id   :de
                 :name "deutsch"}
                {:id   :es
                 :name "español"}
                {:id   :fr
                 :name "français"}])

(def grid-breakpoints [{:xs 4, :sm 3, :md 2, :lg 1}
                       {:xs 6, :sm 4, :md 3, :lg 2}
                       {:xs 12, :sm 6, :md 4, :lg 3}
                       {:xs 12, :md 6}])
