(ns igniteinator.util.re-frame
  (:require [re-frame.core :as rf]
            [clojure.string :as s]))

(defn <sub-ref [name & args]
  (rf/subscribe (into [name] args)))

(defn <sub [name & args]
  @(apply <sub-ref name args))

(defn >evt [name & args]
  (rf/dispatch (into [name] args)))

;; For events
(defn assoc-ins [m & path-vs]
  (reduce (fn [st [p v]]
            (assoc-in st p v))
    m
    (partition 2 path-vs)))

(defn- keyword->path
  ([kw]
   (keyword->path kw identity))
  ([kw name-fn]
   (let [ns-part   (namespace kw)
         name-part (name-fn (name kw))
         ns-path   (if (nil? ns-part)
                     nil
                     (s/split ns-part #"\."))
         path      (conj ns-path name-part)]
     (mapv keyword path))))

(defn reg-sub-db
  "Register subscription with a (get-in db path).
  (reg-sub-db name path): Register name with path.
  (reg-sub-db path-name): Register path-name with path calculated:
  :foo [:foo]
  :foo/bar [:foo :bar]
  :foo.bar/baz [:foo :bar :baz]"
  ([path-name]
   (reg-sub-db path-name (keyword->path path-name)))
  ([name path]
   (rf/reg-sub
     name
     (fn [db _]
       (get-in db path)))))

(defn reg-event-db-assoc
  "Register event with a (assoc-in db path val).
  (reg-sub-db name path): Register name with path.
  (reg-sub-db path-name): Register path-name with path calculated:
  :foo [:foo]
  :foo/bar [:foo :bar]
  :foo/set-bar [:foo :bar]
  :foo.bar/baz [:foo :bar :baz]
  :foo.bar/set-baz [:foo :bar :baz]"
  ([path-name]
   (reg-event-db-assoc path-name (keyword->path path-name
                                   #(s/replace % #"^set-(.+)" "$1"))))
  ([name path]
   (rf/reg-event-db
     name
     (fn [db [_ val]]
       (assoc-in db path val)))))
