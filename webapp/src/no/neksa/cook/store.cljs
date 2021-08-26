(ns no.neksa.cook.store
  (:require
   [ajax.core :refer [GET POST]]
   [ajax.edn :refer [edn-response-format edn-request-format]]
   [reagent.core :as r]))

(def state (r/atom {}))

(defmulti emit (fn [& args] (first args)))

(defmethod emit :fetch-recipes [_]
  (GET "/recipes"
       {:response-format (edn-response-format)
        :handler         #(swap! state assoc :recipes %)}))

(defmethod emit :create-new-recipe [_ new-name]
  (POST "/recipes" {:params  {:recipe/name new-name}
                    :format  (edn-request-format)
                    :handler #(emit :fetch-recipes)}))

