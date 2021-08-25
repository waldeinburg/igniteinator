(ns igniteinator.util.debug)
;; Macros must not be in a CLJS file.
;; WARNING: Merely setting debug? compiler option will not work.
;; You have to do a lein clean or touch the affected file first because the
;; compiler will see that it has not changed and not recompile.

(defn debug? []
  (and cljs.env/*compiler*
    (when-let [{:keys [options]} @cljs.env/*compiler*]
      (:debug? options))))

(defmacro dbg [& msg]
  "Writing a message to console if debug? compiler option is true."
  (if (debug?)
    `(js/console.log "[DEBUG]" ~@msg)))

(defmacro when-debug [& body]
  "Evaluate code if debug? compiler option is true."
  (if (debug?)
    `(do ~@body)))
