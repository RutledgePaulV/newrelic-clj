(ns newrelic-clj.api-test
  (:require [clojure.test :refer :all]
            [newrelic-clj.api :refer :all]
            [ring.adapter.jetty :as jetty]))


(defn-traced dogs [request]
  (Thread/sleep 2000)
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body>Hello!</body></html>"})

(defn-traced bats [request]
  (Thread/sleep 1000)
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<html><head></head><body>Hello!</body></html>"})

(defn-traced trot [request]
  (throw (ex-info "Error" {})))

(defn hello [request]
  ((rand-nth [dogs bats trot]) request))

(defn goodbye [request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    "{\"ok\": true}"})

(defn four-o-four [request]
  {:status  404
   :headers {"Content-Type" "text/plain"}
   :body    "Not found!"})

(def routes
  {[:get "/hello"]   hello
   [:get "/goodbye"] goodbye
   [:get "/*"]       four-o-four})

(defn router [{:keys [request-method uri] :as req}]
  ((or (get routes [request-method uri])
       (get routes [request-method "/*"])
       (get routes [:* uri])
       (get routes [:* "/*"]))
   req))

(def wrapped
  (-> router
      (wrap-transaction-naming)
      (wrap-rum-injection)
      (wrap-transaction)))


(defn run []
  (jetty/run-jetty #'wrapped {:port 3000 :join? false}))