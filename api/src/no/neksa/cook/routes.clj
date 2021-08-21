(ns no.neksa.cook.routes
  (:require
   [ring.util.response :refer [response content-type bad-request]]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]
   [no.neksa.cook.db.recipe :as db])
  (:import
   [org.owasp.html Sanitizers]))

(def ^:private html-policy (.and
                             Sanitizers/FORMATTING
                             Sanitizers/BLOCKS))

(defn- edn-response [body]
  (-> body
      prn-str
      response
      (content-type "application/edn")))

(defn- create-recipe [{recipe :edn-params}]
  (let [recipe (st/select-spec :recipe/recipe-new recipe)]
    (if (s/valid? :recipe/recipe-new recipe)
      (-> recipe
          (update :recipe/directions #(.sanitize html-policy %))
          db/create-recipe
          deref
          edn-response)
      (bad-request (s/explain-str :recipe/recipe-new recipe)))))

(defn- edit-recipe [{recipe :edn-params}]
  (let [recipe (st/select-spec :recipe/recipe recipe)]
    (if (or true(s/valid? :recipe/recipe recipe))
      (-> recipe
          (update :recipe/directions #(.sanitize html-policy %))
          db/edit-recipe
          deref
          edn-response)
      (bad-request (s/explain-str :recipe/recipe recipe)))))

(defn- get-recipe [{{id :id} :route-params}]
  (-> id
      db/get-recipe-by-id
      edn-response))

(defn- get-recipes [{:keys [query-params]}]
  (edn-response
    (if-let [query (get query-params "search")]
      (db/search-recipes-by-name query)
      (db/get-recipes))))

(defn- csrf [req]
  (edn-response (:anti-forgery-token req)))

(defroutes app
  (GET "/csrf" [] csrf)
  (GET "/recipes/:id" [id] get-recipe)
  (GET "/recipes" [] get-recipes)
  (PUT "/recipes" [] edit-recipe)
  (POST "/recipes" [] create-recipe)
  (route/not-found "404 Not found"))

