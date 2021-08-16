(ns no.neksa.cook.middleware.edn
  (:require
   [clojure.edn :as edn])
  (:import
   [java.io InputStream PushbackReader InputStreamReader]))

(defprotocol EdnRead
  (read-edn [this]))

(extend-type String
  EdnRead
  (read-edn [s]
    (edn/read-string s)))

(extend-type InputStream
  EdnRead
  (read-edn [is]
    (edn/read {:eof nil}
              (PushbackReader.
                (InputStreamReader. is "UTF-8")))))

(defn- edn-request? [req]
  (let [content-type (get-in req [:headers "content-type"])]
    (= content-type "application/edn")))

(defn wrap-edn-params [handler]
  (fn [req]
    (if-let [body (and (edn-request? req) (:body req))]
      (handler (assoc req :edn-params (read-edn body)))
      (handler req))))

