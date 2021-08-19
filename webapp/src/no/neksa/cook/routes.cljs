(ns no.neksa.cook.routes
  (:require
   [reagent.core :as r]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]
   [no.neksa.cook.page.home :refer [home-page]]
   [no.neksa.cook.page.shopping-list :refer [shopping-list-page]]
   [no.neksa.cook.page.recipe :refer [recipe-page]]))

(defonce route (r/atom nil))

(def routes
  [["/" {:name  :home
         :label "Oppskrifter"
         :view  #'home-page}]
   ["/shopping-list" {:name  :shopping-list
                      :label "Handleliste"
                      :view  #'shopping-list-page}]
   ["/recipes/:id" {:name       :recipe
                    :view       #'recipe-page
                    :parameters {:path {:id string?}}}]])

(defn start-router! []
  (rfe/start!
    (rf/router routes {:data {:coercion rss/coercion}})
    #(reset! route %)
    {:use-fragment true}))
