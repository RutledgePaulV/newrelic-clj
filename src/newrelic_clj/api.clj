(ns newrelic-clj.api
  (:require [newrelic-clj.internals :as internals]
            [newrelic-clj.inject :as inject]
            [clojure.string :as strings])
  (:import (com.newrelic.api.agent NewRelic Trace Token)))


(deftype DispatchThunk [callback]
  Callable
  (^{Trace {:dispatcher true}} call [_] (callback)))

(deftype AsyncThunk [callback]
  Callable
  (^{Trace {:async true}} call [_] (callback)))

(deftype TraceThunk [callback]
  Callable
  (^{Trace {}} call [_] (callback)))

(defn transaction* [f]
  (.call (->DispatchThunk f)))

(defn async-transaction* [f]
  (.call (->AsyncThunk f)))

(defn trace* [f]
  (.call (->TraceThunk f)))

(defmacro transaction [& body]
  `(transaction* (^{:once true} fn* [] ~@body)))

(defmacro async-transaction [& body]
  `(async-transaction* (^{:once true} fn* [] ~@body)))

(defmacro trace [& body]
  `(trace* (^{:once true} fn* [] ~@body)))

(defn set-transaction-name
  ([name]
   (set-transaction-name nil name))
  ([category name]
   (NewRelic/setTransactionName category name)))

(defn set-parameters
  ([params]
   (set-parameters "" params))
  ([prefix params]
   (NewRelic/addCustomParameters (internals/prefix-keys prefix params))))

(defn create-async-token ^Token []
  (.getToken (.getTransaction (NewRelic/getAgent))))

(defn ignore-current-transaction []
  (NewRelic/ignoreTransaction))

(defn ignore-current-transaction-apdex []
  (NewRelic/ignoreApdex))

(defn defn-traced [& defnargs]
  `(doto (defn ~@defnargs)
     (alter-var-root (fn [old] (fn [& args] (trace (apply old args)))))))

(defn wrap-transaction
  "Middleware to start a newrelic transaction if one doesn't exist and to capture
   basic information about the request."
  [handler]
  (fn wrap-transaction-handler
    ([request]
     (transaction
       (let [req      (internals/adapt-ring-request request)
             response (handler request)
             res      (internals/adapt-ring-response response)]
         (NewRelic/setRequestAndResponse req res)
         response)))
    ([request respond raise]
     (transaction
       (let [token (create-async-token)]
         (handler request
                  (fn respond-callback [response]
                    (async-transaction
                      (.linkAndExpire token)
                      (let [req (internals/adapt-ring-request request)
                            res (internals/adapt-ring-response response)]
                        (NewRelic/setRequestAndResponse req res)
                        (respond response))))
                  (fn raise-callback [^Throwable exception]
                    (async-transaction
                      (.linkAndExpire token)
                      (NewRelic/noticeError exception)
                      (raise exception)))))))))


(defn wrap-transaction-naming
  "Middleware to assign a name to the current transaction based on the route
   that is accessed. Template urls are used instead of the raw uri where possible
   to improve transaction grouping. Template urls are detected for applications using
   compojure or reitit and falls back to just using plain uri. The NewRelic agent applies
   its own grouping of transactions under the hood and so even raw uris may be grouped."
  [handler]
  (fn wrap-transaction-naming-handler
    ([request]
     (let [name (internals/extract-path-template request)]
       (set-transaction-name "Uri" name)
       (handler request)))
    ([request respond raise]
     (let [name (internals/extract-path-template request)]
       (set-transaction-name "Uri" name)
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
            footer   (NewRelic/getBrowserTimingFooter)
            response (handler request)]
        (if (and (or (not (strings/blank? header))
                     (not (strings/blank? footer)))
                 (inject/should-inject? response))
          (inject/perform-injection response header footer)
          response)))
     ([request respond raise]
      (let [header (NewRelic/getBrowserTimingHeader)
            footer (NewRelic/getBrowserTimingFooter)]
        (handler request
                 (fn [response]
                   (respond
                     (if (and (or (not (strings/blank? header))
                                  (not (strings/blank? footer)))
                              (inject/should-inject? response))
                       (inject/perform-injection response header footer)
                       response)))
                 raise))))))