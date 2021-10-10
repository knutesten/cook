(ns no.neksa.cook.page.home
  (:require
   [goog.string :as gstring]
   [clojure.string :as str]
   [no.neksa.cook.store :refer [emit state]]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]))

(def recipes (r/cursor state [:recipes]))

(defn- recipe-list [recipes]
  [:ul
   (for [recipe recipes
         :let   [id (:crux.db/id recipe)
                 href (rfe/href :recipe {:id id})
                 recipe-name (:recipe/name recipe)
                 total-time (+ (:recipe/prep-time recipe)
                               (:recipe/cook-time recipe))]]
     ^{:key id}
     [:li [:a {:href href} recipe-name
           (when-not (zero? total-time)
             (str " (" total-time " min)"))]])])

(defn home-page [_]
  (emit :fetch-recipes)
  (fn []
    (r/with-let [new-name (r/atom "")]
      [:<>
       [:h1 "Det gule husets oppskrifter"]
       [:form {:on-submit #(do (.preventDefault %)
                               (emit :create-new-recipe @new-name)
                               (reset! new-name ""))}
        [:input {:type      "text"
                 :value     @new-name
                 :on-change #(reset! new-name (-> % .-target .-value))}]
        (gstring/unescapeEntities "&nbsp;")
        [:button {:type     "submit"
                  :disabled (str/blank? @new-name)} "Lag ny"]]
       (recipe-list @recipes)])))

