(ns io.github.rutledgepaulv.newrelic-clj.api
  (:require [io.github.rutledgepaulv.newrelic-clj.internals :as internals]
            [io.github.rutledgepaulv.newrelic-clj.inject :as inject]
            [io.github.rutledgepaulv.newrelic-clj.types :as types]
            [clojure.string :as strings]
            [clojure.walk :as walk])
  (:import (com.newrelic.api.agent NewRelic Transaction TransactionNamePriority TracedMethod Token)
           (org.slf4j MDC)
           (java.util HashMap Map)))

(set! *warn-on-reflection* true)

(defn get-transaction
  "Get the current transaction."
  ^Transaction []
  (.getTransaction (NewRelic/getAgent)))

(defn in-transaction?
  "Check if there's already an active transaction"
  ([]
   (in-transaction? (get-transaction)))
  ([^Transaction transaction]
   (not= "com.newrelic.agent.bridge.NoOpTransaction" (.getName (class transaction)))))

(defn set-transaction-request
  "Set a ring request map as the web request for the current transaction."
  ([request]
   (set-transaction-request (get-transaction) request))
  ([^Transaction transaction request]
   (.setWebRequest transaction (internals/adapt-ring-request request))))

(defn set-transaction-response
  "Set a ring request map as the web response for the current transaction."
  ([response]
   (set-transaction-response (get-transaction) response))
  ([^Transaction transaction response]
   (.setWebResponse transaction (internals/adapt-ring-response response))))

(defn set-transaction-name
  "Set a transaction category and name for the current transaction."
  ([^String category ^String name]
   (set-transaction-name (get-transaction) category name))
  ([^Transaction transaction ^String category ^String name]
   (.setTransactionName transaction TransactionNamePriority/CUSTOM_HIGH true category (into-array String [name]))))

(defn omit-transaction
  "Don't report the current transaction"
  ([]
   (omit-transaction (get-transaction)))
  ([^Transaction transaction]
   (.ignore transaction)))

(defn omit-transaction-from-apdex
  "Don't report the current transaction against the apdex"
  ([]
   (omit-transaction-from-apdex (get-transaction)))
  ([^Transaction transaction]
   (.ignoreApdex transaction)))

(defn omit-transaction-errors
  "Don't report errors on the current transaction"
  ([]
   (omit-transaction-errors (get-transaction)))
  ([^Transaction transaction]
   (.ignoreErrors transaction)))

(defn get-trace
  "Get the current trace."
  ([]
   (get-trace (get-transaction)))
  ([^Transaction transaction]
   (.getTracedMethod transaction)))

(defn get-linking-context
  "Returns a map of data necessary to include in logs"
  []
  (or (.getLinkingMetadata (NewRelic/getAgent)) {}))

(defn set-trace-name
  "Set a category and name for the current trace."
  ([^String category ^String name]
   (set-trace-name (get-trace) category name))
  ([^TracedMethod trace ^String category ^String name]
   (.setMetricName trace (into-array String [category name]))))

(defn set-trace-params
  "Set parameters onto the current trace"
  ([prefix params]
   (set-trace-params (get-transaction) prefix params))
  ([^TracedMethod trace prefix params]
   (.addCustomAttributes trace (internals/prefix-keys prefix params))))

(defn get-async-token
  "Get a token that can be used to connect traces back to a single transaction
   even if the traces run on different threads."
  (^Token []
   (get-async-token (get-transaction)))
  (^Token [^Transaction transaction]
   (.getToken transaction)))

(defn notice-error
  "Reports an error to newrelic against the current transaction."
  ([error]
   (cond
     (instance? Throwable error)
     (NewRelic/noticeError ^Throwable error)
     (string? error)
     (NewRelic/noticeError ^String error)
     :otherwise
     (throw (IllegalArgumentException. "Not a type that can be reported to newrelic as an error."))))
  ([error data]
   (cond
     (instance? Throwable error)
     (NewRelic/noticeError ^Throwable error ^Map (walk/stringify-keys data))
     (string? error)
     (NewRelic/noticeError ^String error ^Map (walk/stringify-keys data))
     :otherwise
     (throw (IllegalArgumentException. "Not a type that can be reported to newrelic as an error.")))))


(defn mdc-context-fn
  "Returns an instrumented version of f which will first establish
   a mdc context containing the data from the context map."
  [f context]
  (if (not-empty context)
    (fn [& args]
      (let [original (MDC/getCopyOfContextMap)]
        (try
          (let [modifiable (or (MDC/getCopyOfContextMap) (HashMap.))]
            (.putAll ^Map modifiable (walk/stringify-keys context))
            (MDC/setContextMap modifiable)
            (apply f args))
          (finally
            (MDC/setContextMap (or original (HashMap.)))))))
    f))

(defmacro with-mdc-linking
  "Sets up a mdc context containing the attributes necessary for 'logs in context' before executing body."
  [& body]
  `((mdc-context-fn (^{:once true} fn* [] ~@body) (get-linking-context))))

(defn transaction-fn
  "Instrument a function to produce a new function that will report to newrelic
   as a transaction if there is no parent transaction and as a child trace if
   there is already a parent transaction."
  ([f]
   (types/->TransactionFn
     (fn [& args]
       (try
         (with-mdc-linking
           (apply f args))
         (catch Exception e
           (notice-error e)
           (throw e))))))
  ([f category name]
   (fn [& args]
     (let [nested (in-transaction?)]
       (apply
         (types/->TransactionFn
           (fn [& args]
             (try
               (if-not nested
                 (set-transaction-name category name)
                 (set-trace-name category name))
               (with-mdc-linking
                 (apply f args))
               (catch Exception e
                 (notice-error e)
                 (throw e)))))
         args)))))

(defn async-transaction-fn
  "Instrument a function to produce a new function that will report to newrelic
   as a transaction if there is no parent transaction and as a child trace if
   there is already a parent transaction."
  ([f]
   (types/->AsyncTransactionFn
     (fn [& args]
       (try
         (with-mdc-linking
           (apply f args))
         (catch Exception e
           (notice-error e)
           (throw e))))))
  ([f category name]
   (fn [& args]
     (let [nested (in-transaction?)]
       (apply
         (types/->AsyncTransactionFn
           (fn [& args]
             (try
               (if-not nested
                 (set-transaction-name category name)
                 (set-trace-name category name))
               (with-mdc-linking
                 (apply f args))
               (catch Exception e
                 (notice-error e)
                 (throw e)))))
         args)))))

(defmacro with-transaction
  "Runs body in a newrelic transaction if there is no parent transaction
   or as a trace if there already is a parent transaction."
  [& body]
  `((transaction-fn (^{:once true} fn* [] ~@body))))

(defmacro with-async-transaction
  "Runs body in a newrelic async transaction if there is no parent transaction
   or as a trace if there already is a parent transaction."
  [& body]
  `((async-transaction-fn (^{:once true} fn* [] ~@body))))

(defmacro defn-traced
  "Like defn, but for defining functions that will report as newrelic transactions
   if there is no parent transaction, or as a trace if there already is a parent
   transaction."
  [& defnargs]
  `(let [var# (defn ~@defnargs)]
     (doto var# (alter-var-root transaction-fn "Clojure" (str (symbol var#))))))

(defn wrap-transaction
  "Ring middleware to establish a web transaction if one doesn't exist."
  [handler]
  (fn wrap-transaction-handler
    ([request]
     (with-transaction
       (.convertToWebTransaction (get-transaction))
       (set-transaction-request request)
       (doto (handler request)
         (set-transaction-response))))
    ([request respond raise]
     (with-transaction
       (.convertToWebTransaction (get-transaction))
       (set-transaction-request request)
       (let [token (get-async-token)]
         (handler request
                  (fn respond-callback [response]
                    (with-async-transaction
                      (.linkAndExpire token)
                      (set-transaction-response response)
                      (respond response)))
                  (fn raise-callback [^Throwable exception]
                    (with-async-transaction
                      (.linkAndExpire token)
                      (NewRelic/noticeError exception)
                      (raise exception)))))))))


(defn wrap-transaction-naming
  "Middleware to assign a name to the current transaction based on the route that is accessed.
   Template urls are used instead of the raw uri where possible to improve transaction grouping.
   Template urls are detected for applications using compojure or reitit and falls back to just
   using plain uri. The NewRelic agent applies its own grouping of transactions under the hood
   and so even raw uris may be grouped."
  [handler]
  (fn wrap-transaction-naming-handler
    ([request]
     (let [[category name] (internals/extract-path-template request)]
       (set-transaction-name category name)
       (handler request)))
    ([request respond raise]
     (let [[category name] (internals/extract-path-template request)]
       (set-transaction-name category name)
       (handler request respond raise)))))


(defn wrap-rum-injection
  "Middleware to detect html page responses and inject a preconfigured client side rum agent."
  ([handler]
   (wrap-rum-injection handler {}))
  ([handler {:keys [should-inject?]
             :or   {should-inject? inject/should-inject?}}]
   (fn wrap-rum-injection-handler
     ([request]
      (let [header   (NewRelic/getBrowserTimingHeader)
            response (handler request)]
        (if (and (not (strings/blank? header))
                 (should-inject? response))
          (inject/perform-injection response header)
          response)))
     ([request respond raise]
      (let [header (NewRelic/getBrowserTimingHeader)]
        (handler request
                 (fn [response]
                   (respond
                     (if (and (not (strings/blank? header))
                              (should-inject? response))
                       (inject/perform-injection response header)
                       response)))
                 raise))))))
