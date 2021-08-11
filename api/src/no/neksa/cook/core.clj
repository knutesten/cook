(ns no.neksa.cook.core
  (:require
   [mount.core :refer [defstate start]]
   [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defstate server
  :start (run-jetty (fn [req] {:status  200
                               :headers {"Content-Type" "text/plain"}
                               :body    (str req)})
                    {:port  3030
                     :join? false})
  :stop (.stop server))

(defn -main [& args]
  (start))
