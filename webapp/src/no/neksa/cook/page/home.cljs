(ns no.neksa.cook.page.home
  (:require
   [goog.string :as gstring]
   [ajax.core :refer [GET POST]]
   [ajax.edn :refer [edn-response-format edn-request-format]]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]))

(defonce state (r/atom {:recipes []}))

(defn- recipe-list [recipes]
  [:ul
   (for [recipe recipes
         :let   [id (:crux.db/id recipe)
                 href (rfe/href :recipe {:id id})
                 recipe-name (:recipe/name recipe)]]
     ^{:key id}
     [:li [:a {:href href} recipe-name]])])

(defn fetch-recipes []
  (GET "/recipes"
       {:response-format (edn-response-format)
        :handler         #(swap! state assoc :recipes %)}))

(defn create-recipe-handler [evt]
  (.preventDefault evt)
  (POST "/recipes" {:params  {:recipe/name (:new-recipe-name @state)}
                    :format  (edn-request-format)
                    :handler (fn [_]
                               (swap! state dissoc :new-recipe-name)
                               (fetch-recipes))}))

(defn home-page [_]
  (fetch-recipes)
  (fn []
    (let [{:keys [new-recipe-name recipes]} @state]
      [:<>
       [:h1 "Det gule husets oppskrifter"]
       [:form {:on-submit create-recipe-handler}
        [:input {:type      "text"
                 :value     new-recipe-name
                 :on-change #(swap! state assoc :new-recipe-name (-> % .-target .-value))}]
        (gstring/unescapeEntities "&nbsp;")
        [:button {:type     "submit"
                  :disabled (not new-recipe-name)} "Lag ny"]]
       (recipe-list recipes)])))

