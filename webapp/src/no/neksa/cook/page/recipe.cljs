(ns no.neksa.cook.page.recipe
  (:require
   [reitit.frontend.easy :as rfe]
   [no.neksa.cook.store :refer [recipe emit units]]))

(defn- ingredients []
  [:<>
   [:h3 "Ingredienser"]
   [:h6 "Porsjoner"]
   [:input {:type      "text"
            :style     {:width "45px"}
            :on-change #(emit :change-portions (.. % -target -value))
            :value     (or (:portions-selected @recipe)
                           (:recipe/portions @recipe))}]
   [:ul
    (let [factor (or (:factor @recipe) 1)]
      (for [[idx ing] (zipmap (range) (:recipe/ingredients @recipe))
            :let      [amount (:ingredient/amount ing)
                       unit (:ingredient/unit ing)
                       name (:ingredient/name ing)]]
        ^{:key idx}
        [:li (* factor amount) " " (units unit) " " name]))]])

(defn- cook-time []
  (let [prep-time (:recipe/prep-time @recipe)
        cook-time (:recipe/cook-time @recipe)]
    [:div
     (when prep-time
       [:span "Forberedelser: " prep-time " min"])
     (when (and prep-time cook-time)
       [:span " | "])
     (when cook-time
       [:span "Koketid: " cook-time " min"])
     (when (and prep-time cook-time)
       [:div
        [:b "Total tid: " (+ prep-time cook-time) " min"]])]))

(defn recipe-page [{{{id :id} :path} :parameters}]
  (emit :fetch-recipe id)
  (fn []
    (when @recipe
      [:<>
       [:h1 (:recipe/name @recipe)]
       (when-let [desc (:recipe/description @recipe)]
         [:blockquote desc])
       [cook-time]
       [ingredients]
       [:div
        [:h3 "Framgangsmåte"]
        [:p {:dangerouslySetInnerHTML
             {:__html (:recipe/directions @recipe)}}]]
       [:a {:href (rfe/href :edit-recipe {:id id})}
        "Endre på oppskrift"]])))


