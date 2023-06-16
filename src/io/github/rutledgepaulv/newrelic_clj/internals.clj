(ns io.github.rutledgepaulv.newrelic-clj.internals
  (:require [clojure.string :as strings])
  (:import (com.newrelic.api.agent ExtendedRequest ExtendedResponse HeaderType)
           (java.util.regex Matcher Pattern)))

(set! *warn-on-reflection* true)

(defprotocol ContentLength
  (byte-length [x]))

(extend-protocol ContentLength
  (Class/forName "[B")
  (byte-length [x] (alength ^bytes x))
  nil
  (byte-length [x] 0)
  Object
  (byte-length [x] -1)
  String
  (byte-length [x] (alength (.getBytes x))))

(def status-code-mapping
  {100 "Continue"
   101 "Switching Protocols"
   200 "OK"
   201 "Created"
   202 "Accepted"
   203 "Non-Authoritative Information"
   204 "No Content"
   205 "Reset Content"
   206 "Partial Content"
   300 "Multiple Choices"
   301 "Moved Permanently"
   302 "Found"
   303 "See Other"
   304 "Not Modified"
   305 "Use Proxy"
   307 "Temporary Redirect"
   400 "Bad Request"
   401 "Unauthorized"
   402 "Payment Required"
   403 "Forbidden"
   404 "Not Found"
   405 "Method Not Allowed"
   406 "Not Acceptable"
   407 "Proxy Authentication Required"
   408 "Request Timeout"
   409 "Conflict"
   410 "Gone"
   411 "Length Required"
   412 "Precondition Failed"
   413 "Request Entity Too Large"
   414 "Request-URI Too Long"
   415 "Unsupported Media Type"
   416 "Requested Range Not Satisfiable"
   417 "Expectation Failed"
   500 "Internal Server Error"
   501 "Not Implemented"
   502 "Bad Gateway"
   503 "Service Unavailable"
   504 "Gateway Timeout"
   505 "HTTP Version Not Supported"})

(defn adapt-ring-request [ring-request]
  (proxy [ExtendedRequest] []
    (getHeaders [name]
      (if-some [header (get-in ring-request [:headers name])]
        (strings/split header #",")
        []))
    (getHeaderType []
      HeaderType/HTTP)
    (getHeader [name]
      (get-in ring-request [:headers name]))
    (getMethod []
      (some-> ring-request :request-method name strings/upper-case))
    (getRequestURI []
      (:uri ring-request))))

(defn adapt-ring-response [ring-response]
  (proxy [ExtendedResponse] []
    (getContentLength []
      (or (when-some [header (or (get-in ring-response [:headers "Content-Length"])
                                 (get-in ring-response [:headers "content-length"]))]
            (Long/parseLong header))
          (byte-length (:body ring-response))))
    (getStatus []
      (or (:status ring-response) 200))
    (getStatusMessage []
      (get status-code-mapping (.getStatus ^ExtendedResponse this)))
    (getContentType []
      (or (get-in ring-response [:headers "Content-Type"])
          (get-in ring-response [:headers "content-type"])
          "application/octet-stream"))))

(defn map-keys
  "Transform the keys of a map"
  [f m]
  (letfn [(f* [agg k v] (assoc! agg (f k) v))]
    (with-meta
      (persistent! (reduce-kv f* (transient (or (empty m) {})) m))
      (meta m))))

(defn prefix-keys [prefix m]
  (map-keys (fn [k] (str prefix "/" (name k))) m))

(defn path-pattern [route-params]
  (let [interior (map (fn [[k v]] (str "(" (Pattern/quote v) ")")) route-params)]
    (re-pattern (str "/" (strings/join "(?:/|/.+/)" interior) "(?:/|$)"))))

(defn re-groups' [^Matcher m]
  (let [gc (. m (groupCount))]
    (if (zero? gc)
      (. m (group))
      (loop [ret [] c 0]
        (if (<= c gc)
          (recur (conj ret [(. m (group c)) (.start m c) (.end m c)]) (inc c))
          ret)))))

(defn replace-by [^String s re f]
  (let [m (re-matcher re s)]
    (if (.find m)
      (let [buffer (StringBuffer. (.length s))]
        (loop [found true]
          (if found
            (do (.appendReplacement m buffer (Matcher/quoteReplacement (f (re-groups' m))))
                (recur (.find m)))
            (do (.appendTail m buffer)
                (.toString buffer)))))
      s)))

(defn reverse-route-params [uri route-params]
  (let [pattern      (path-pattern route-params)
        replacements (into [] (seq route-params))]
    (replace-by
      uri
      pattern
      (fn [[[original-match original-offset _] & groups]]
        (:match
          (reduce
            (fn [{:keys [match offset]} [index [^String group start end]]]
              (let [replacement (key (get replacements index))
                    new-match   (str (subs match 0 (+ offset start))
                                     (str replacement)
                                     (subs original-match end))
                    new-offset  (+ offset (- (.length ^String (str replacement)) (.length group)))]
                {:match new-match :offset new-offset}))
            {:offset 0 :match original-match}
            (for [[i [group start end]] (map-indexed vector groups)]
              [i [group (- start original-offset) (- end original-offset)]])))))))

(defn extract-path-template [request]
  (try
    (cond
      (and (contains? request :context)
           (string? (:context request))
           (contains? request :compojure/route))
      ["NormalizedUri" (reverse-route-params (:uri request) (:route-params request))]
      (contains? request :compojure/route)
      ["NormalizedUri" (some-> request :compojure/route (nth 1))]
      (contains? request :reitit.core/match)
      ["NormalizedUri" (some-> request :reitit.core/match :template)]
      :otherwise
      ["Uri" (:uri request)])
    (catch Exception e
      ["Uri" (:uri request)])))