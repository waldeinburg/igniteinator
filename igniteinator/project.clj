(defproject igniteinator "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.844"]
                 [org.clojure/core.async "1.3.618"]
                 [cljs-http "0.1.46"]
                 [arttuka/reagent-material-ui "4.11.3-2"
                  :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server arttuka/reagent-material-ui-js]]]

  :source-paths ["src"]

  :aliases {"fig"      ["trampoline" "run" "-m" "figwheel.main"]
            "fig:dev"  ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"  ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test" ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "igniteinator.test-runner"]}

  :profiles {:dev {:dependencies   [[com.bhauman/figwheel-main "0.2.13" :exclusions [org.clojure/clojurescript]]
                                    [com.bhauman/rebel-readline-cljs "0.1.4"]
                                    [org.clojure/math.combinatorics "0.1.6"]]
                   :resource-paths ["target"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets  ^{:protect false} ["target"]}})
