(ns no.neksa.cook.db
  (:require
   [mount.core :refer [defstate]]
   [clojure.java.io :as io]
   [crux.api :as crux]
   [no.neksa.cook.config :refer [config]]))

(defn start-crux! []
  (letfn [(kv-store [dir]
            {:kv-store {:crux/module 'crux.rocksdb/->kv-store
                        :db-dir      (io/file dir)
                        :sync?       true}})]
    (crux/start-node
      {:crux/tx-log         (kv-store (:crux/tx-log config))
       :crux/document-store (kv-store (:crux/document-store config))
       :crux/index-store    (kv-store (:crux/index-store config))})))

(defstate crux-node
  :start (start-crux!)
  :stop (.close crux-node))

