
<img src="./docs/logo.png" title="newrelic-clj" width="300" height="300" align="left" padding="5px"/>
<small>
<br/><br/><br/><br/>
A library to simplify usage of the Newrelic APM Java API from Clojure programs.
</small>
<br clear="all" /><br />

---

## Rationale

NewRelic's APM agent instruments many Java libraries to capture observability data simply by running their JVM agent.
Unfortunately, the APM agent is not particularly "Clojure Friendly" as it relies on Java Annotations or XML pointcuts to 
instrument custom code. This library seeks to bridge the gap and make working with NewRelic from Clojure easier and 
more idiomatic.

---

## Usage

You'll find examples below of how to use `newrelic-clj` to instrument your code.

### Creating web transactions

Use `wrap-transactions` to make sure that every web request creates a Newrelic transaction including basic information
about the request. This middleware should be placed on the absolute exterior of your application.

```clojure

(require '[newrelic-clj.api :as nr])
(require '[ring.adapter.jetty :as jetty])

(defn handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body><p>Hello!</p></body></html>"})

(def wrapped (nr/wrap-transaction handler))

(jetty/run-jetty #'wrapped {:port 3000 :join? false})

```

### Naming Web Transactions (Compojure)

Compojure injects match data into requests before they reach any of your handlers. To achieve nice transaction
names you want to send requests through `newrelic-clj.api/wrap-transaction-naming` immediately after routing has 
occurred. The easiest way to do that is to use `compojure.core/wrap-routes` middleware to enqueue middleware to 
execute after routing.

```clojure

(require '[newrelic-clj.api :as nr])
(require '[ring.adapter.jetty :as jetty])
(require '[compojure.core :as cr])

(cr/defroutes app 
  (cr/GET "/" []
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    "<html><head></head><body><p>Hello!</p></body></html>"}))

(def wrapped
  (-> app
      (route/wrap-routes nr/wrap-transaction-naming)
      (nr/wrap-transaction)))

(jetty/run-jetty #'wrapped {:port 3000 :join? false})

```

### Naming Web Transactions (Reitit)

Reitit injects match data into requests before they reach any of your handlers. To achieve nice transaction
names you want to send requests through `newrelic-clj.api/wrap-transaction-naming` on every route. The easiest
way to do that is to use the `:reitit.middleware/transform` option when compiling a router to inject this 
middleware on every endpoint (after routing).

```clojure

(require '[newrelic-clj.api :as nr])
(require '[ring.adapter.jetty :as jetty])
(require '[reitit.ring :as rr])

(defn handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body><p>Hello!</p></body></html>"})

(defn not-found [request]
  {:status  404
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body><p>Not found!</p></body></html>"})

; making sure this middleware is applied to all endpoints

(defn mw-transform [mw]
  (into [nr/wrap-transaction-naming] mw))

(def router
  (rr/router
    [["/" {:get handler}]]
    {:reitit.middleware/transform mw-transform}))

(def wrapped
  (let [opts {:middleware [nr/wrap-transaction]}]
    (rr/ring-handler router not-found opts)))

(jetty/run-jetty #'wrapped {:port 3000 :join? false})

```

### Injecting Client Side Real User Monitoring (RUM)

You can use `newrelic-clj.api/wrap-rum-injection` middleware to add NewRelic script tags to all html page responses. Injection 
is performed by [injecting-streams](https://github.com/RutledgePaulV/injecting-streams) and takes place as bytes are written 
to the response stream. The performance overhead is very low (< 10μs).

```clojure

(require '[newrelic-clj.api :as nr])
(require '[ring.adapter.jetty :as jetty])

(defn handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body><p>Hello!</p></body></html>"})

(def wrapped
  (-> handler
      (nr/wrap-rum-injection)
      (nr/wrap-transaction)))

(jetty/run-jetty #'wrapped {:port 3000 :join? false})


```

### Creating function traces

It's common to track timing / invocation counts for activity other than web requests. You may turn 
any Clojure function into a unique transaction / span.

```clojure

(require '[newrelic-clj.api :as nr])

(defn my-sum [coll]
  (reduce + 0 coll))

; long form with custom categories / names
(def my-traced-sum 
  (nr/transaction-fn run "Clojure" "user/my-sum"))

; syntax sugar (w/ automatic naming)
(nr/defn-traced my-traced-sum [coll]
  (reduce + 0 coll))

; if newrelic agent is active, this will start a transaction named using the fully qualified symbol
; if this function is called when there is already an active transaction it will just show as a child span
(my-traced-sum (range 1000000))

```

### Noticing errors

You can report an error against a running transaction so that it's surfaced in NewRelic.

```clojure

(require '[newrelic-clj.api :as nr])

(try
  (do-dangerous-thing)
  (catch Exception e
    (nr/notice-error e)))

```

### Logs In Context

This library connects NewRelic's "linking data" to SLF4J's MDC to propagate tracing identifiers into application logs.
If you're using NewRelic's logging collectors and configure your appenders to output json this means you'll be able to 
see relationships between APM spans and the logs generated during those spans.


---

### License

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).
