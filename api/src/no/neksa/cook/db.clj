(ns no.neksa.cook.db
  (:require
   [mount.core :refer [defstate]]
   [clojure.java.io :as io]
   [crux.api :as crux]))

(defn start-crux! []
  (letfn [(kv-store [dir]
            {:kv-store {:crux/module 'crux.rocksdb/->kv-store
                        :db-dir      (io/file dir)
                        :sync?       true}})]
    (crux/start-node
      {:crux/tx-log         (kv-store "data/dev/tx-log")
       :crux/document-store (kv-store "data/dev/doc-store")
       :crux/index-store    (kv-store "data/dev/index-store")})))

(defstate crux-node
  :start (start-crux!)
  :stop (.close crux-node))

