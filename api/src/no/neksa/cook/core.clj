(ns no.neksa.cook.core
  (:require
   [mount.core :refer [defstate start]]
   [nrepl.server :as nrepl]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [no.neksa.cook.spec]
   [no.neksa.cook.routes :refer [app]]
   [no.neksa.cook.middleware.edn :refer [wrap-edn-params]])
  (:gen-class))

(defstate nrepl
  :start (nrepl/start-server :port 4000
                             :bind "127.0.0.1")
  :stop (nrepl/stop-server nrepl))

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
