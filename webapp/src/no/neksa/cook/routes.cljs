(ns no.neksa.cook.routes
  (:require
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]
   [no.neksa.cook.store :refer [emit]]
   [no.neksa.cook.page.home :refer [home-page]]
   [no.neksa.cook.page.shopping-list :refer [shopping-list-page]]
   [no.neksa.cook.page.recipe :refer [recipe-page]]
   [no.neksa.cook.page.edit-recipe :refer [edit-recipe-page]]))

(def routes
  [["/" {:name  :home
         :label "Oppskrifter"
         :view  #'home-page}]
   ["/shopping-list" {:name  :shopping-list
                      :label "Handleliste"
                      :view  #'shopping-list-page}]
   ["/recipes/:id" {:name       :recipe
                    :view       #'recipe-page
                    :parameters {:path {:id string?}}}]
   ["/recipes/:id/edit" {:name       :edit-recipe
                         :view       #'edit-recipe-page
                         :parameters {:path {:id string?}}}]])

(defn start-router! []
  (rfe/start!
    (rf/router routes {:data {:coercion rss/coercion}})
    #(emit :change-route %)
    {:use-fragment true}))

