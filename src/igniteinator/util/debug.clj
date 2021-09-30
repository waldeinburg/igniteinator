(ns igniteinator.util.debug)
;; Macros must be in a CLJ file. Due to the use of cljs.env/*compiler*, compilation as CLJS (which also happens with
;; CLJC ending) will fail.
;; WARNING: Merely setting debug? compiler option will not work. You have to do a lein clean or touch the affected file
;; first because the compiler will see that it has not changed and not recompile.

(defn compiler-option? [k]
  (and cljs.env/*compiler*
    (when-let [{:keys [options]} @cljs.env/*compiler*]
      (get options k))))

(defn dev? []
  (compiler-option? :dev?))

(defn debug? []
  (compiler-option? :debug?))

(defmacro dbg [& msg]
  "Writing a message to console if debug? compiler option is true."
  (if (debug?)
    `(js/console.log "[DEBUG]" ~@msg)))

(defmacro when-debug [& body]
  "Evaluate code if debug? compiler option is true."
  (if (debug?)
    `(do ~@body)))

(defmacro if-dev [then else]
  "Evaluate then or else depending on if dev? compiler option is true."
  (if (dev?)
    then
    else))

(defmacro when-dev [& body]
  "Evaluate code if dev? compiler option is true."
  (if (dev?)
    `(do ~@body)))
