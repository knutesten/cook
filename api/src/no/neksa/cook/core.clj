(ns no.neksa.cook.core
  (:require
   [mount.core :refer [defstate start]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [no.neksa.cook.spec]
   [no.neksa.cook.routes :refer [app]]
   [no.neksa.cook.middleware.edn :refer [wrap-edn-params]])
  (:gen-class))

(defstate server
  :start (run-jetty (-> app
                        (wrap-edn-params)
                        ;;(wrap-defaults site-defaults)
                        )
                    {:port  3030
                     :join? false})
  :stop (.stop server))

(defn -main [& args]
  (start))
