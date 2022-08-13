(ns igniteinator.app-server
  (:require
    [clojure.java.io :refer [file resource]]
    [ring.util.response :refer [content-type not-found resource-response]]))

;; Use the same pattern as .htaccess
(def path-pattern (let [htaccess (-> "public/.htaccess" resource file slurp)
                        path-re  (second (re-find #"\n# Routing.*\nRewriteRule \^([^ ]+)" htaccess))]
                    ;; mod_rewrite is matched against the path without leading slash.
                    (re-pattern (str "/" path-re))))

(defn handler [req]
  (or
    (when (re-find path-pattern (:uri req))
      (some-> (resource-response "index.html" {:root "public"})
        (content-type "text/html; charset=utf-8")))
    (not-found "Not found")))
