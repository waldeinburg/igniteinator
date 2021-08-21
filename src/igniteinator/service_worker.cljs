(ns igniteinator.service-worker
  (:require [igniteinator.constants :as constants]
            [clojure.string :as str]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.string :as str]))
;; Cf. https://github.com/gja/pwa-clojure/blob/master/src-svc/pwa_clojure/service_worker.cljs
;; and the following and related articles.
;; https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Offline_Service_workers

(def image-cache-version 1)
(def app-cache-name (str "igniteinator-data-v" constants/version))
(def image-cache-name (str "igniteinator-img-v" image-cache-version))
(def app-cache-files
  ["/index.html"
   "/manifest.webmanifest"
   "/img/placeholder.png"
   "/css/style.css"
   constants/data-file-path])

(defn dbgmsg [msg]
  (js/console.log (str "[Service Worker] " msg)))

(defn- purge-old-caches []
  (dbgmsg "Purging old caches")
  (->
    js/caches
    .keys
    (.then (fn [keys]
             (->> keys
               (map #(when-not (#{app-cache-name image-cache-name} %)
                       (.delete js/caches %)))
               clj->js
               js/Promise.all)))))

;; TODO: Change when state is persisted and multiple languages are supported.
(defn- current-language []
  :en)

(defn- cache-image-list [cache]
  (let [response (<! (http/get constants/data-file-path))]
    (if (:success response)
      (.addAll cache (map #(str constants/gen-img-base-path
                             "/"
                             (name (current-language))
                             "/"
                             (:id %)
                             constants/gen-img-ext)
                       (get-in response [:body :cards])))
      (dbgmsg (str "Failed to load data: " (:error-text response))))))

(defn- install-service-worker []
  (dbgmsg "Installing service worker")
  (->
    js/caches
    (.open app-cache-name)
    (.then (fn [cache]
             (.addAll cache app-cache-files))))
  (when (.. js/self -navigator -standalone)
    (dbgmsg "Adding images to cache")
    (-> js/caches
      (.open image-cache-name)
      (.then cache-image-list))))

(defn- fetch-and-cache [request]
  (let [response (js/fetch request)
        uri      (-> request .-url http/parse-url :uri)
        image?   (str/starts-with? uri constants/gen-img-base-path)]
    (->
      js/caches
      (.open (if image? image-cache-name app-cache-name))
      (.put request (.clone response)))
    response))

(defn- fetch-event [e]
  (let [request (.-request e)]
    (->
      js/caches
      (.match request)
      (.then (fn [response]
               (or response (js/fetch request)))))))

(.addEventListener js/self "install" #(.waitUntil % (install-service-worker)))
(.addEventListener js/self "fetch" #(.respondWith % (fetch-event %)))
(.addEventListener js/self "activate" #(.waitUntil % (purge-old-caches)))
