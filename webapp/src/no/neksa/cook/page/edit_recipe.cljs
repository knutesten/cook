(ns no.neksa.cook.page.edit-recipe
  (:require
   [goog.string :as gstring]
   [reitit.frontend.easy :as rfe]
   ["react-quill" :as ReactQuill]
   [no.neksa.cook.form-util :refer [input-text-with-label textarea-with-label]]
   [no.neksa.cook.store :refer [recipe units emit]]))

(def nbsp (gstring/unescapeEntities "&nbsp;"))

(defn- name-input []
  [input-text-with-label
   (:recipe/name @recipe)
   "Navn"
   #(emit :set-recipe-name %)])

(defn- portions-input []
  [input-text-with-label
   (:recipe/portions @recipe)
   "Porsjoner"
   #(emit :set-recipe-portions %)])

(defn- prep-time-input []
  [input-text-with-label
   (:recipe/prep-time @recipe)
   "Forberedelsestid"
   #(emit :set-recipe-prep-time %)])

(defn- cook-time-input []
  [input-text-with-label
   (:recipe/cook-time @recipe)
   "Koketid"
   #(emit :set-recipe-cook-time %)])

(defn- description-textarea []
  [textarea-with-label
   (:recipe/description @recipe)
   "Beskrivelse"
   #(emit :set-recipe-description %)])

(defn- ingredient-name-input [idx]
  [:input
   {:type      "text"
    :value     (:ingredient/name (nth (:recipe/ingredients @recipe) idx))
    :on-change #(emit :set-ingredient-name idx (.. % -target -value))}])

(defn- ingredient-unit-select [idx]
  (let [ing (nth (:recipe/ingredients @recipe) idx)]
    [:select {:value ((fnil name "") (:ingredient/unit ing))
              :on-change
              #(emit :set-ingredient-unit idx (-> % .-target .-value))}
     [:option {:value ""}]
     (for [[u t] units]
       ^{:key u}
       [:option {:value (name u)} t])]))

(defn- ingredient-amount-input [idx]
  (let [ing (nth (:recipe/ingredients @recipe) idx)]
    [:input
     {:type      "text"
      :value     (or (:amount-formatted ing) (:ingredient/amount ing))
      :on-change #(emit :set-ingredient-amount idx (.. % -target -value))}]))

(defn- ingredients-input []
  [:<>
   [:label "Ingredienser"]
   [:ul
    (for [idx (range (count (:recipe/ingredients @recipe)))]
      ^{:key idx}
      [:li
       [ingredient-amount-input idx] nbsp
       [ingredient-unit-select idx] nbsp
       [ingredient-name-input idx] nbsp
       nbsp
       [:button {:on-click #(emit :move-ingredient-up idx)} "???"] nbsp
       [:button {:on-click #(emit :move-ingredient-down idx)} "???"] nbsp
       [:button {:on-click #(emit :delete-ingredient idx)} "-"] nbsp])
    [:li
     [:button {:on-click #(emit :create-ingredient)} "+"]]]])

(defn edit-recipe-page [{{{id :id} :path} :parameters}]
  (emit :fetch-recipe id)
  (fn []
    (when @recipe
      [:<>
       [:h1 "Endrer p?? " (:recipe/name @recipe)]
       [name-input]
       [description-textarea]
       [prep-time-input]
       [cook-time-input]
       [portions-input]
       [ingredients-input]
       [:label "Framangsm??te"]
       [:> ReactQuill
        {:theme     "snow"
         :value     (:recipe/directions @recipe)
         :on-change #(swap! recipe assoc :recipe/directions %)}]
       [:br]
       [:button {:on-click #(rfe/push-state :recipe {:id id})} "Avbryt"]
       nbsp
       [:button {:type     "submit"
                 :on-click #(emit :save-recipe)} "Lagre"]])))

