(ns no.neksa.cook.db.recipe
  (:require
   [clojure.spec.alpha :as s]
   [crux.api :as crux]
   [no.neksa.cook.db :refer [crux-node]])
  (:import
   [java.util UUID]))

(defn get-recipe-by-id [id]
  (let [result (crux/pull (crux/db crux-node) '[*] id)]
    (when-not (empty? result)
      result)))

(defn create-recipe
  [recipe]
  {:pre [(s/valid? :recipe/recipe-new recipe)]}
  (let [id  (.toString (UUID/randomUUID))
        doc (merge recipe {:crux.db/id id})
        tx  (crux/submit-tx crux-node [[:crux.tx/put doc]])]
    (future
      (crux/await-tx crux-node tx)
      (get-recipe-by-id id))))
