(ns no.neksa.cook.middleware.webapp
  (:require
   [ring.middleware.file :refer [wrap-file]]))

(defn wrap-webapp [handler]
  (let [file-handler (wrap-file handler "../webapp/public")]
    (fn [req]
      (let [res (file-handler req)]
        (if (= "/" (:uri req))
          (assoc-in res [:headers "content-type"] "text/html")
          res)))))

