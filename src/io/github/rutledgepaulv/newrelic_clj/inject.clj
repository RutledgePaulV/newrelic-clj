(ns io.github.rutledgepaulv.newrelic-clj.inject
  (:require [clojure.string :as strings]
            [ring.core.protocols :as protos])
  (:import (com.github.rutledgepaulv.injectingstreams InjectingStreams)
           (java.io OutputStream InputStream ByteArrayInputStream)
           (java.util.zip GZIPOutputStream GZIPInputStream DeflaterOutputStream InflaterInputStream)))

(set! *warn-on-reflection* true)

(defn get-content-type [response]
  (or (get-in response [:headers "Content-Type"])
      (get-in response [:headers "content-type"])
      "application/octet-stream"))

(defn get-content-encoding [response]
  (or (get-in response [:headers "Content-Encoding"])
      (get-in response [:headers "content-encoding"])
      ""))

(defn get-content-length [response]
  (when-some [length
              (or (get-in response [:headers "Content-Length"])
                  (get-in response [:headers "content-length"]))]
    (Long/parseLong length)))

(defn should-inject? [response]
  (let [content-type (get-content-type response)]
    (strings/starts-with? content-type "text/html")))

(defn gunzip [body]
  (cond
    (instance? InputStream body)
    (GZIPInputStream. body)
    (bytes? body)
    (recur (ByteArrayInputStream. body))
    :otherwise
    (throw (ex-info (str "Don't know how to gunzip from " (class body)) {:body body}))))

(defn inflate [body]
  (cond
    (instance? InputStream body)
    (InflaterInputStream. body)
    (bytes? body)
    (recur (ByteArrayInputStream. body))
    :otherwise
    (throw (ex-info (str "Don't know how to inflate from " (class body)) {:body body}))))

(defn inject-output-stream ^OutputStream [^OutputStream output-stream ^String header]
  (cond-> output-stream
    (not (strings/blank? header))
    (InjectingStreams/injectAfterOutput "<head>" header)))

(defn dispatch [response _]
  (->> (strings/split (get-content-encoding response) #"\s*,\s*")
       (filterv (complement strings/blank?))))

(defmulti perform-injection #'dispatch)

; default to no injection if content-encoding unsupported
(defmethod perform-injection :default [response _]
  response)

(defmethod perform-injection [] [{:keys [body] :as response} ^String header]
  (let [current-length (get-content-length response)
        extra-length   (alength (.getBytes header))
        injected-body  (reify protos/StreamableResponseBody
                         (write-body-to-stream [_ response output-stream]
                           (with-open [injected-stream (inject-output-stream output-stream header)]
                             (protos/write-body-to-stream body response injected-stream))))]
    (cond-> response
      (some? current-length)
      (assoc-in [:headers "Content-Length"] (str (+ current-length extra-length)))
      :always
      (assoc :body injected-body))))

(defmethod perform-injection ["gzip"] [{:keys [body] :as response} ^String header]
  (let [injected-body
        (reify protos/StreamableResponseBody
          (write-body-to-stream [_ response output-stream]
            (with-open [compressed-stream
                        (-> output-stream
                            (GZIPOutputStream.)
                            (inject-output-stream header))]
              (protos/write-body-to-stream (gunzip body) response compressed-stream))))]
    (-> response
        (update :headers dissoc "Content-Length" "content-length")
        (update :headers assoc "Content-Encoding" "gzip")
        (assoc :body injected-body))))

(defmethod perform-injection ["deflate"] [{:keys [body] :as response} ^String header]
  (let [injected-body
        (reify protos/StreamableResponseBody
          (write-body-to-stream [_ response output-stream]
            (with-open [compressed-stream
                        (-> output-stream
                            (DeflaterOutputStream.)
                            (inject-output-stream header))]
              (protos/write-body-to-stream (inflate body) response compressed-stream))))]
    (-> response
        (update :headers dissoc "Content-Length" "content-length")
        (update :headers assoc "Content-Encoding" "deflate")
        (assoc :body injected-body))))
