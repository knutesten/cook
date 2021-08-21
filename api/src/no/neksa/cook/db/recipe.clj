(ns no.neksa.cook.db.recipe
  (:require
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [crux.api :as crux]
   [no.neksa.cook.db :refer [crux-node]])
  (:import
   [java.util UUID Date]))

(defn get-recipe-by-id [id]
  (let [id     (if (uuid? id) id (UUID/fromString id))
        result (crux/pull (crux/db crux-node) '[*] id)]
    (when-not (empty? result)
      result)))

(defn create-recipe
  [recipe]
  {:pre [(s/valid? :recipe/recipe-new recipe)]}
  (let [id  (UUID/randomUUID)
        now (Date.)
        doc (merge recipe {:crux.db/id          id
                           :recipe/updated-inst now
                           :recipe/created-inst now})
        tx  (crux/submit-tx crux-node [[:crux.tx/put doc]])]
    (future
      (crux/await-tx crux-node tx)
      (get-recipe-by-id id))))

(defn edit-recipe
  [recipe]
  {:pre [(s/valid? :recipe/recipe recipe)]}
  (let [now    (Date.)
        recipe (assoc recipe :recipe/updated-inst now)
        tx     (crux/submit-tx crux-node [[:crux.tx/put recipe]])]
    (future
      (crux/await-tx crux-node tx)
      (get-recipe-by-id (:crux.db/id recipe)))))

(defn get-recipes []
  (map first
       (crux/q
         (crux/db crux-node)
         '{:find     [(pull id [*]) updated-inst]
           :where    [[id :recipe/name]
                      [id :recipe/updated-inst updated-inst]]
           :order-by [[updated-inst :desc]]})))

(defn search-recipes-by-name [query]
  (map first
       (crux/q
         (crux/db crux-node)
         '{:find  [(pull id [*])]
           :in    [query]
           :where [[id :recipe/name n]
                   [(str/includes? n query)]]}
         query)))

