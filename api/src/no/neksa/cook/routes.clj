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

(defroutes app
  (PUT "/recipe" [] edit-recipe)
  (POST "/recipe" [] create-recipe)
  (route/not-found "404 Not found"))

