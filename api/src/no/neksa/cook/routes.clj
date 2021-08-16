(ns no.neksa.cook.routes
  (:require
   [ring.util.response :refer [response content-type bad-request]]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [no.neksa.cook.spec.util :as su]
   [no.neksa.cook.db.recipe :as db]))

(defn- edn-response [body]
  (-> body
      prn-str
      response
      (content-type "application/edn")))

(defn- create-recipe [{recipe :edn-params}]
  (if (su/valid-closed? :recipe/recipe-new recipe)
    (-> recipe
        db/create-recipe
        deref
        edn-response)
    (bad-request "Invalid recipe")))

(defn- edit-recipe [{recipe :edn-params}]
  (if (su/valid-closed? :recipe/recipe recipe)
    (-> recipe
        db/edit-recipe
        deref
        edn-response)
    (bad-request "Invalid recipe")))

(defn- get-recipe [{{id :id} :route-params}]
  (-> id
      db/get-recipe-by-id
      edn-response))

(defn- get-recipes [{:keys [query-params]}]
  (if-let [query (get query-params "search")]
    (db/search-recipes-by-name query)
    (db/get-recipes)))

(defroutes app
  (GET "/recipe/:id" [id] get-recipe)
  (GET "/recipe" [] get-recipes)
  (PUT "/recipe" [] edit-recipe)
  (POST "/recipe" [] create-recipe)
  (route/not-found "404 Not found"))

