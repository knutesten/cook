(ns no.neksa.cook.page.recipe
  (:require
   [ajax.core :refer [GET]]
   [ajax.edn :refer [edn-response-format]]
   [reagent.core :as r]))

(defn recipe-page [{{{id :id} :path} :parameters}]
  (r/with-let
    [recipe (r/atom nil)
     _ (GET (str "/recipes/" id)
            {:response-format (edn-response-format)
             :handler         #(reset! recipe %)})]
    [:h1 (:recipe/name @recipe)]))
