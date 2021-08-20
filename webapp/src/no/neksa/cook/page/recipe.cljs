(ns no.neksa.cook.page.recipe
  (:require
   [ajax.core :refer [GET PUT]]
   [ajax.edn :refer [edn-response-format edn-request-format]]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]))

(defonce recipe (r/atom nil))

(defn fetch-recipe [id]
  (GET (str "/recipes/" id)
       {:response-format (edn-response-format)
        :handler         #(reset! recipe %)}))

(defn save-recipe [evt]
  (.preventDefault evt)
  (PUT "/recipes" {:params  @recipe
                   :format  (edn-request-format)
                   :handler #(rfe/push-state
                               :recipe
                               {:id (:crux.db/id @recipe)})}))

(defn input-text-with-label [data key label]
  [:label
   label
   [:br]
   [:input {:type      "text"
            :value     (key  @data)
            :on-change #(swap! data assoc key (-> % .-target .-value))}]])

(defn textarea-with-label [data key label]
  [:label
   label
   [:br]
   [:textarea {:on-change #(swap! data assoc key (-> % .-target .-value))
               :value     (key  @data)}]])

(defn edit-recipe-page [{{{id :id} :path} :parameters}]
  (fetch-recipe id)
  (fn []
    [:form
     [:h1 "Endrer på " (:recipe/name @recipe)]
     [input-text-with-label recipe :recipe/name "Navn"]
     [:br]
     [textarea-with-label recipe :recipe/description "Beskrivelse"]
     [:br]
     [:button {:type     "submit"
               :on-click save-recipe} "Lagre"]]))

(defn recipe-page [{{{id :id} :path} :parameters}]
  (fetch-recipe id)
  (fn []
    [:<>
     [:h1 (:recipe/name @recipe)]
     [:a {:href (rfe/href :edit-recipe {:id id})}
      "Endre på oppskrift"]
     [:blockquote (:recipe/description @recipe)]]))
