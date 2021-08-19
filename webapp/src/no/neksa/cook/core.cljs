(ns no.neksa.cook.core
  (:require
   [ajax.core :refer [GET]]
   [ajax.edn :refer [edn-response-format]]
   [reagent.core :as r]
   [reagent.dom :as d]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]))

(defn recipe-list [recipes]
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

(defn recipe-page [{{{id :id} :path} :parameters}]
  (r/with-let
    [recipe (r/atom nil)
     _ (GET (str "/recipes/" id)
            {:response-format (edn-response-format)
             :handler         #(reset! recipe %)})]
    [:h1 (:recipe/name @recipe)]))

(defn shopping-list-page [_]
  [:h1 "Kommer snart ..."])

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

(defn nav-link [route-name link-text current-route]
  [:a {:href (rfe/href route-name)}
   (if (= route-name (-> current-route :data :name))
     [:b link-text]
     link-text)])

(defn navbar [current-route]
  [:nav
   (interpose
     ^{:key "|"}[:span " | "]
     (for [[_ r] routes
           :let  [route-name (:name r)
                  link-text (:label r)]
           :when link-text]
       ^{:key route-name}
       [nav-link route-name link-text current-route]))])

(defn layout-page []
  (let [current-route @route
        view          (-> current-route :data :view)]
    (when view
      [:<>
       [navbar current-route]
       [:section
        [view current-route]]])))

(defn mount-root []
  (d/render [layout-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (rfe/start!
    (rf/router routes {:data {:coercion rss/coercion}})
    #(reset! route %)
    {:use-fragment true})
  (mount-root))
