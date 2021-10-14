(ns igniteinator.util.string-test
  (:require [clojure.test :refer [deftest are testing]]
            [igniteinator.util.string :refer [format]]))

(deftest format-test
  (are [s res]
    (= (format s {:str "foo", :num 42}) res)
    "foo" "foo"
    "\\{str}bar" "{str}bar"
    "bar\\{str}baz" "bar{str}baz"
    "{str}\\{str}bar" "foo{str}bar"
    "{str}bar" "foobar"
    "bar{str}" "barfoo"
    "bar{str}baz" "barfoobaz"
    "{str}{num}bar" "foo42bar"
    "bar{str}{num}" "barfoo42"
    "bar{str}{num}baz" "barfoo42baz"
    "bar{str}baz{num}quz" "barfoobaz42quz"))
