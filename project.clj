(defproject igniteinator "0.1.0-SNAPSHOT"
  :description "An unoffical app for the board game Ignite"
  :url "https://igniteinator.waldeinburg.dk"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.844"]
                 [org.clojure/core.async "1.3.618"]
                 [funcool/promesa "6.0.2"]
                 [cljs-http "0.1.46"]
                 [arttuka/reagent-material-ui "4.11.3-2"
                  :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server arttuka/reagent-material-ui-js]]]

  :source-paths ["src"]

  :aliases {"fig"          ["trampoline" "run" "-m" "figwheel.main"]
            "fig:dev"      ["trampoline" "run" "-m" "figwheel.main"
                            "--build" "main" "--repl"]
            ;; Cf. sw.cljs.edn for the problems.
            ;; And without advanced optimizations it does not seem to compile properly anyway.
            ;;"fig:dev-sw"   ["trampoline" "run" "-m" "figwheel.main" "-b" "sw" "-r"]
            "fig:build"    ["run" "-m" "figwheel.main"
                            "--optimizations" "advanced"
                            "--build-once" "main"]
            "fig:build-sw" ["run" "-m" "figwheel.main"
                            "--optimizations" "advanced"
                            "--build-once" "sw"]
            "fig:debug-sw" ["run" "-m" "figwheel.main"
                            "--compile-opts" {:debug? true}
                            "--optimizations" "simple"
                            "--build-once" "sw"]
            "fig:test"     ["run" "-m" "figwheel.main"
                            "--compile-opts" "test.cljs.edn"
                            "-m" "igniteinator.test-runner"]}

  :profiles {:dev {:dependencies   [[com.bhauman/figwheel-main "0.2.13" :exclusions [org.clojure/clojurescript]]
                                    [com.bhauman/rebel-readline-cljs "0.1.4"]
                                    [org.clojure/math.combinatorics "0.1.6"]]
                   :resource-paths ["target"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets  ^{:protect false} ["target"]}})
