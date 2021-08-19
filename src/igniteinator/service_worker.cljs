(ns igniteinator.service-worker
  (:require [igniteinator.constants :refer [version]]))
;; Cf. https://github.com/gja/pwa-clojure/blob/master/src-svc/pwa_clojure/service_worker.cljs
;; and the following and related articles.
;; https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Offline_Service_workers

(defn dbgmsg [msg]
  (js/console.log (str "[Service Worker] " msg)))

(defn- purge-old-caches []
  (dbgmsg "purge-old-caches stub"))

(defn- install-service-worker []
  (dbgmsg "install-service-worker stub"))

(defn- fetch-event [e]
  ;; no caching implemented yet
  (js/fetch (.-request e)))

(.addEventListener js/self "install" #(.waitUntil % (install-service-worker)))
(.addEventListener js/self "fetch" #(.respondWith % (fetch-event %)))
(.addEventListener js/self "activate" #(.waitUntil % (purge-old-caches)))
