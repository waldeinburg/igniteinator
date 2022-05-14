(defproject igniteinator "0.1.0-SNAPSHOT"
  :description "An unoffical app for the board game Ignite"
  :url "https://igniteinator.waldeinburg.dk"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.4"]
                 [funcool/promesa "8.0.450"]
                 [cljs-http "0.1.46"]
                 [re-frame "1.2.0"
                  :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [day8.re-frame/http-fx "0.2.4"]
                 [akiroz.re-frame/storage "0.1.4"]
                 [arttuka/reagent-material-ui "5.6.2-0"
                  :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server
                               arttuka/reagent-material-ui-js]]]

  :source-paths ["src"]

  :aliases {"fig"          ["trampoline" "run" "-m" "figwheel.main"]
            "fig:dev"      ["trampoline" "run" "-m" "figwheel.main"
                            "--build" "main" "--repl"]
            "fig:build"    ["run" "-m" "figwheel.main"
                            "--optimizations" "advanced"
                            "--build-once" "prod"]
            ;; Cleans service worker compile folder, not target/public/sw.js.
            "fig:clean-sw" ["run" "-m" "figwheel.main" "--clean" "sw"]
            ;; No sw build without optimizations (cf. sw.cljs.edn).
            "fig:build-sw" ["run" "-m" "figwheel.main"
                            "--optimizations" "advanced"
                            "--fw-opts" {:final-output-to "target/final/sw.js"}
                            "--build-once" "sw"]
            ;; Service worker dev-mode without cache.
            "fig:dev-sw"   ["run" "-m" "figwheel.main"
                            "--compile-opts" {:dev? true}
                            "--fw-opts" {:final-output-to "target/public/sw.js"}
                            "--optimizations" "simple"
                            "--build-once" "sw"]
            ;; Not :dev? true; this is debugging the real behavior.
            "fig:debug-sw" ["run" "-m" "figwheel.main"
                            "--compile-opts" {:debug? true}
                            "--fw-opts" {:final-output-to "target/public/sw.js"}
                            "--optimizations" "simple"
                            "--build-once" "sw"]
            "fig:test"     ["run" "-m" "figwheel.main"
                            "--compile-opts" "test.cljs.edn"
                            "-m" "igniteinator.test-runner"]}

  :profiles {:dev {:dependencies   [[com.bhauman/figwheel-main "0.2.17" :exclusions [org.clojure/clojurescript]]
                                    [com.bhauman/rebel-readline-cljs "0.1.4"]
                                    [org.clojure/math.combinatorics "0.1.6"]
                                    [day8.re-frame/tracing "0.6.2"]
                                    [day8.re-frame/re-frame-10x "1.2.7"]
                                    [binaryage/devtools "1.0.6"]]
                   :resource-paths ["target"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets  ^{:protect false} ["target"]}})
