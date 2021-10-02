(ns igniteinator.service-worker
  (:require [igniteinator.constants :as constants]
            [igniteinator.util.image-path :refer [image-path]]
            [igniteinator.util.message :as msg]
            [clojure.string :as str]
            [promesa.core :as p]
            [cljs-http.client :refer [parse-url]])
  (:require-macros [igniteinator.util.debug :refer [dbg when-debug when-dev if-dev]]))
;; Cf. https://github.com/gja/pwa-clojure/blob/master/src-svc/pwa_clojure/service_worker.cljs
;; and the following and related articles.
;; https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps/Offline_Service_workers

(def image-cache-version 2)
(def app-cache-name (str "igniteinator-data-v" constants/version))
(def image-cache-name (str "igniteinator-img-v" image-cache-version))

(def app-cache-files
  [;; Avoid the boilerplate and just cache index twice; it's small anyway.
   "/"
   "/index.html"
   "/main.js"
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

(defn post-msg [client-id msg-type msg-data]
  (dbg "Getting client to post message")
  (p/let [client (->
                   (.-clients js/self)
                   (.get client-id))]
    (dbg "Post message" (name msg-type) (clj->js msg-data))
    (msg/post client msg-type msg-data)))

(defn match-cache [request]
  (.match js/caches request
    ;; Without ignoreVary, requests from browser will miss images cached by URL string.
    (clj->js {"ignoreVary" true})))

(defn add-to-cache [request response]
  (let [url            (if (string? request)
                         request
                         (.-url request))
        path           (-> url parse-url :uri)
        image?         (str/starts-with? path (str constants/gen-img-base-path "/"))
        response-clone (.clone response)]
    (if-dev
      (dbg "Ignore caching" path)
      (do
        (dbg "Caching" request)
        (p/let [cache (.open js/caches
                        (if image? image-cache-name app-cache-name))]
          (.put cache request response-clone))))))

(defn fetch-and-cache [request]
  (dbg "Fetch" request "for caching")
  (p/let [response (js/fetch request)]
    (if (.-ok response)
      (add-to-cache request response)
      (warn "Error response! Not caching." request response))
    response))

(defn fetch-request [request]
  (dbg "Fetch" request)
  (p/let [response (match-cache request)]
    (dbg "Cache" (if response "hit" "miss") request)
    (or response (fetch-and-cache request))))

(defn get-data-and-cache-image-list [language client-id]
  (p/let [response        (fetch-request constants/data-file-path)
          data            (p/let [json (.json response)]
                            (js->clj json :keywordize-keys true))
          ;; We cannot use filter because match-cache returns a Promise, not a boolean.
          uncached-paths! (reduce (fn [prev-promise card]
                                    (let [request (image-path language card)]
                                      (p/let [result prev-promise
                                              match  (match-cache request)]
                                        (if match
                                          result
                                          (conj! result request)))))
                            (p/promise (transient []))
                            (:cards data))]
    ;; Chain the requests to fetch only one at a time. Let the user have the bandwidth for using the app, possibly
    ;; hitting images that are not cached. Thus, fetch-request will once again check if the image is cached.
    (if-let [uncached-paths (not-empty (persistent! uncached-paths!))]
      (let [cnt (count uncached-paths)]
        ;; Let the stated message post before starting, but then don't care about the message sync.
        ;; There will be no simultaneous downloads anyway.
        (reduce (fn [prev-promise request]
                  (p/let [prev-result prev-promise]
                    ;; The first item will get 0 from the initial noop promise. This will signal start of caching.
                    ;; The last item will get the final message sent and ignore the request (nil).
                    ;; Would a loop-recur have been prettier? Maybe.
                    (post-msg client-id :img-caching-progress {:progress prev-result
                                                               :count    cnt})
                    (if request
                      (p/then (fetch-request request)
                        #(inc prev-result)))))
          (p/resolved 0)
          ;; Add nil at the end to ensure sending the last message.
          (conj uncached-paths nil))))))

(defn fetch-event [e]
  (dbg "Fetch event" e)
  (fetch-request (.-request e)))

(defn purge-old-caches []
  (info "Purging old caches")
  (p/let [keys (.keys js/caches)]
    (->> keys
      (map #(when-not (#{app-cache-name image-cache-name} %)
              (.delete js/caches %)))
      clj->js
      p/all)))

(defn clear-app-cache []
  (info "Clearing app cache")
  (.delete js/caches app-cache-name))

(defn install-service-worker []
  (info "Installing service worker")
  (dbg "Debug messages are on!")
  (if-dev
    (dbg "No caching")
    ;; Add cache busting query parameter to avoid fetching from some other cache.
    ;; https://github.com/GoogleChrome/samples/blob/gh-pages/service-worker/prefetch/service-worker.js
    (let [now                  (. js/Date now)
          add-cache-bust-param (fn [url]
                                 ;; We don't prefetch anything with query parameters so just use a naive implementation.
                                 (str url "?cache-bust=" now))]
      (p/let [cache (.open js/caches app-cache-name)]
        (doseq [path app-cache-files]
          (p/let [response (js/fetch (add-cache-bust-param path))]
            (if (.-ok response)
              ;; Add without the caching bust parameter.
              (.put cache path response)
              (warn "Error response! Not caching." path response))))))))

(defn handle-mode [mode client-id]
  (info "Mode is" (name mode))
  (when (= mode :standalone)
    (dbg "Standalone mode. Ensure images in cache.")
    ;; At installation we would want to cache the default language if in standalone mode. However, the service worker is
    ;; already installed at the first visit. Instead, the client messages the service worker and the worker ensures that
    ;; the images are in the cache.
    ;; TODO: At language change, receive message with the current language.
    (get-data-and-cache-image-list constants/default-language client-id)))

(def handle-message (msg/message-handler
                      (fn [msg-type msg-data e]
                        (let [client-id (.. e -source -id)]
                          (condp = msg-type
                            :mode (handle-mode (keyword msg-data) client-id)
                            :cache-clear (clear-app-cache)
                            (warn "Invalid message type" (if (keyword? msg-type)
                                                           (name msg-type)
                                                           msg-type)))))))

(.addEventListener js/self "install" #(.waitUntil % (install-service-worker)))
(.addEventListener js/self "activate" #(.waitUntil % (purge-old-caches)))
;; The fetch event handler is not working when the service worker is installed. We could probably fix this:
;; https://stackoverflow.com/a/34608364
;; However, that is probably for the best. It means that we don't cache images unless the user has visited us twice.
;; Thus, image caching depends on user engagement.
(.addEventListener js/self "fetch" #(.respondWith % (fetch-event %)))
(.addEventListener js/self "message" handle-message)

(when-dev
  (info "Dev mode"))
