(ns no.neksa.cook.page.home
  (:require
   [ajax.core :refer [GET]]
   [ajax.edn :refer [edn-response-format]]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]))

(defn- recipe-list [recipes]
  [:ul
   (for [recipe recipes
         :let   [id (:crux.db/id recipe)
                 href (rfe/href :recipe {:id id})
                 recipe-name (:recipe/name recipe)]]
     ^{:key id}
     [:li [:a {:href href} recipe-name]])])

(defn home-page [_]
  (r/with-let
    [recipes (r/atom [])
     _ (GET "/recipes"
            {:response-format (edn-response-format)
             :handler         #(reset! recipes %)})]
    [:<>
     [:h1 "Det gule husets oppskrifter"]
     (recipe-list @recipes)]))

