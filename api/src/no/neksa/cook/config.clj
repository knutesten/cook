(ns no.neksa.cook.config
  (:require
   [clojure.edn :as edn]
   [clojure.tools.logging :as log]
   [mount.core :refer [defstate]]))

(defn- read-config [file]
  (try
    (edn/read-string (slurp file))
    (catch Exception e
      (log/warn "Could not load config file" file)
      nil)))

(defstate config
  :start (merge (read-config "config.edn")
                (read-config "secret.edn")))
