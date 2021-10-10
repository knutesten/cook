(ns no.neksa.cook.store
  (:require
   [clojure.string :as str]
   [ajax.core :refer [GET POST PUT]]
   [ajax.edn :refer [edn-response-format edn-request-format]]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]))

(def state (r/atom {}))

(def recipe (r/cursor state [:recipe]))

(def units
  {:ml "ml" :dl "dl" :l "l" :g "g" :kg "kg" :tsp "ts" :tbsp "ss" :clove "fedd"})

(defn- vec-del [coll idx]
  (into (subvec coll 0 idx) (subvec coll (inc idx))))

(defn- vec-up [coll idx]
  (if (zero? idx)
    coll
    (into (subvec coll 0 (dec idx))
          (concat
            [(nth coll idx)]
            [(nth coll (dec idx))]
            (subvec coll (inc idx))))))

(defn- vec-down [coll idx]
  (if (= (count coll) (inc idx))
    coll
    (into (subvec coll 0 idx)
          (concat
            [(nth coll (inc idx))]
            [(nth coll idx)]
            (subvec coll (+ 2 idx))))))

(defmulti emit (fn [& args] (first args)))

(defmethod emit :fetch-recipes [_]
  (GET "/recipes"
       {:response-format (edn-response-format)
        :handler         #(swap! state assoc :recipes %)}))

(defmethod emit :fetch-recipe [_ id]
  (GET (str "/recipes/" id)
       {:response-format (edn-response-format)
        :handler         #(swap! state assoc :recipe %)}))

(defmethod emit :change-portions [_ portions]
  (let [default-portions (get-in @state [:recipe :recipe/portions])]
    (swap! state assoc-in [:recipe :factor] (/ portions default-portions)))
  (swap! state assoc-in [:recipe :portions-selected] portions))

(defmethod emit :set-recipe-name [_ name]
  (swap! state assoc-in [:recipe :recipe/name] name))

(defmethod emit :set-recipe-description [_ desc]
  (swap! state assoc-in [:recipe :recipe/description] desc))

(defmethod emit :set-recipe-portions [_ nr-str]
  (let [nr (js/parseInt nr-str)]
    (cond
      (empty? nr-str)
      (swap! state update :recipe dissoc :recipe/portions)

      (and (int? nr) (pos? nr))
      (swap! state assoc-in [:recipe :recipe/portions] nr))))

(defmethod emit :set-ingredient-name [_ idx name]
  (swap! recipe assoc-in [:recipe/ingredients idx :ingredient/name] name))

(defmethod emit :set-ingredient-unit [_ idx unit-str]
  (if-let [unit (if (= unit-str "") nil (keyword unit-str))]
    (swap! recipe assoc-in [:recipe/ingredients idx :ingredient/unit] unit)
    (swap! recipe update-in [:recipe/ingredients idx] dissoc :ingredient/unit)))

(defmethod emit :set-ingredient-amount [_ idx nr-str]
  (let [nr-str-us (str/replace nr-str "," ".")
        valid?    (re-matches #"[0-9]*\.?[0-9]*" nr-str-us)
        nr        (js/parseFloat nr-str-us)]
    (when valid?
      (if (js/isNaN nr)
        (swap! recipe
               update-in [:recipe/ingredients idx] dissoc :ingredient/amount)
        (swap! recipe
               assoc-in [:recipe/ingredients idx :ingredient/amount] nr))
      (swap! recipe
             assoc-in [:recipe/ingredients idx :amount-formatted] nr-str))))

(defmethod emit :move-ingredient-up [_ idx]
  (swap! recipe update :recipe/ingredients vec-up idx))

(defmethod emit :move-ingredient-down [_ idx]
  (swap! recipe update :recipe/ingredients vec-down idx))

(defmethod emit :delete-ingredient [_ idx]
  (swap! recipe update :recipe/ingredients vec-del idx))

(defmethod emit :create-ingredient [_]
  (swap! recipe update :recipe/ingredients (fnil conj []) {}))

(defmethod emit :save-recipe [_]
  (PUT "/recipes" {:params  @recipe
                   :format  (edn-request-format)
                   :handler #(rfe/push-state
                               :recipe
                               {:id (:crux.db/id @recipe)})}))

(defmethod emit :create-new-recipe [_ new-name]
  (POST "/recipes" {:params  {:recipe/name new-name}
                    :format  (edn-request-format)
                    :handler #(emit :fetch-recipes)}))

(defmethod emit :change-route [_ new-route]
  (swap! state assoc :route new-route))


