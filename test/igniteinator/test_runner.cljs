;; This test runner is intended to be run from the command line
(ns igniteinator.test-runner
  (:require
    ;; require all the namespaces that you want to test
    [figwheel.main.testing :refer [run-tests-async]]
    [igniteinator.core-test]
    [igniteinator.randomizer.randomizer-test]
    [igniteinator.util.sort-test]
    [igniteinator.util.string-test]))

(defn -main [& args]
  (run-tests-async 5000))
