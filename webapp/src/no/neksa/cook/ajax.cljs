(ns no.neksa.cook.ajax
  (:require
   [ajax.core :refer [GET default-interceptors to-interceptor]]
   [ajax.edn :refer [edn-response-format]]))

(defonce csrf-token (atom nil))
(GET "/csrf" {:handler         #(reset! csrf-token %)
              :response-format (edn-response-format)})

(defn add-csrf-token-header [req]
  (if (#{"POST" "PUT"} (:method req))
    (update req :headers assoc "x-csrf-token" @csrf-token)
    req))

(swap! default-interceptors
       conj
       (to-interceptor {:name    "CSRF"
                        :request add-csrf-token-header}))

