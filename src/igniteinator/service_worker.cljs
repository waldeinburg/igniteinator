(ns igniteinator.service-worker
  (:require [igniteinator.constants :as constants]
            [igniteinator.util.image-path :refer [image-path]]
            [clojure.string :as str]
            [clojure.string :as str]
            [goog.Uri :as uri])
  (:require-macros [igniteinator.util.debug :refer [dbg when-debug]]))
;; Cf. https://github.com/gja/pwa-clojure/blob/master/src-svc/pwa_clojure/service_worker.cljs
;; and the following and related articles.
;; https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Offline_Service_workers

(def image-cache-version 1)
(def app-cache-name (str "igniteinator-data-v" constants/version))
(def image-cache-name (str "igniteinator-img-v" image-cache-version))

(def app-cache-files
  [;; Avoid the boilerplate and just cache index twice; it's small anyway.
   "/"
   "/index.html"
   "/cljs-out/main/main_bundle.js"
   "/manifest.webmanifest"
   "/img/placeholder.png"
   "/css/style.css"
   ;; Icons
   "/icons/apple-touch-icon.png"
   "/icons/maskable-192.png"
   "/icons/splash.png"
   constants/data-file-path])

(defn log-msg [logger msgs]
  (apply logger (cons "[Service Worker] " msgs)))

(defn info [& msg]
  (log-msg js/console.info msg))

(defn warn [& msg]
  (log-msg js/console.warn msg))

(defn- add-to-cache [request response]
  (let [url            (if (string? request)
                         request
                         (.-url request))
        ;; Don't use cljs-http.client/parse-url. The library will result in a "...=window;" statement in the compiled
        ;; service worker, but there's no window object in this context.
        path           (-> url uri/parse .getPath)
        image?         (str/starts-with? path (str constants/gen-img-base-path "/"))
        response-clone (.clone response)]
    (dbg "Caching" request)
    (->
      js/caches
      (.open (if image? image-cache-name app-cache-name))
      (.then (fn [cache]
               (.put cache request response-clone))))))

(defn- fetch-and-cache [request]
  (dbg "Fetch" request "for caching")
  (->
    (js/fetch request)
    (.then (fn [response]
             (if (.-ok response)
               (add-to-cache request response)
               (warn "Error response! Not caching." request response))
             response))))

(defn- fetch-request [request]
  (dbg "Fetch" request)
  (->
    js/caches
    (.match request
      ;; Without ignoreVary, requests from browser will miss images cached by URL string.
      (clj->js {"ignoreVary" true}))
    (.then (fn [response]
             (dbg "Cache" (if response "hit" "miss") request)
             (or response (fetch-and-cache request))))))

(defn- cache-image-list-from-json-data [language data]
  (dbg "Data ready. Caching images.")
  (let [data (js->clj data :keywordize-keys true)]
    ;; Do not use Cache.addAll. The function will trigger on each page load, not just service worker installation.
    (doseq [card (:cards data)]
      (fetch-request (image-path language card)))))

(defn- get-data-and-cache-image-list [language]
  (->
    ;; The data should be in the cache by now.
    (fetch-request constants/data-file-path)
    (.then (fn [response]
             (->
               (.json response)
               (.then (partial cache-image-list-from-json-data language)))))))

(defn- fetch-event [e]
  (dbg "Fetch event" e)
  (fetch-request (.-request e)))

(defn- purge-old-caches []
  (info "Purging old caches")
  (->
    (.keys js/caches)
    (.then (fn [keys]
             (->> keys
               (map #(when-not (#{app-cache-name image-cache-name} %)
                       (.delete js/caches %)))
               clj->js
               js/Promise.all)))))

(defn- install-service-worker []
  (info "Installing service worker")
  (dbg "Debug messages are on!")
  (->
    js/caches
    (.open app-cache-name)
    (.then (fn [app-cache]
             (.addAll app-cache (clj->js app-cache-files))))))

(defn- handle-mode [mode]
  (when (= mode :standalone)
    (dbg "Standalone mode. Ensure images in cache.")
    ;; At installation we would want to cache the default language if in standalone mode. However, the service worker is
    ;; already installed at the first visit. Instead, the client messages the service worker and the worker ensures that
    ;; the images are in the cache.
    ;; TODO: At language change, receive message with the current language.
    (get-data-and-cache-image-list constants/default-language)))

(defn- handle-message [e]
  (let [msg      (js->clj (.-data e) :keywordize-keys true)
        msg-type (keyword (:type msg))]
    (info "Received message of type" (name msg-type))
    (condp = msg-type
      :mode (handle-mode (keyword (:value msg)))
      (warn "Invalid message" (.-data e)))))

(.addEventListener js/self "install" #(.waitUntil % (install-service-worker)))
(.addEventListener js/self "activate" #(.waitUntil % (purge-old-caches)))
;; The fetch event handler is not working when the service worker is installed. We could probably fix this:
;; https://stackoverflow.com/a/34608364
;; However, that is probably for the best. It means that we don't cache images unless the user has visited us twice.
;; Thus, image caching depends on user engagement.
(.addEventListener js/self "fetch" #(.respondWith % (fetch-event %)))
(.addEventListener js/self "message" handle-message)
