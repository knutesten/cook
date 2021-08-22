(ns no.neksa.cook.page.recipe
  (:require
   [clojure.string :as str]
   ["react-quill" :as ReactQuill]
   [goog.string :as gstring]
   [ajax.core :refer [GET PUT]]
   [ajax.edn :refer [edn-response-format edn-request-format]]
   [reitit.frontend.easy :as rfe]
   [reagent.core :as r]
   [no.neksa.cook.form-util :refer [input-int-with-label
                                    input-text-with-label
                                    textarea-with-label]]))

(defonce recipe (r/atom nil))

(defn update-recipe! [& vals]
  (swap! recipe (fn [r]
                  (apply (partial update r) vals))))

(defn update-in-recipe! [ks & vals]
  (swap! recipe (fn [r]
                  (apply (partial update-in r ks) vals))))

(defn assoc-in-recipe! [ks val]
  (swap! recipe assoc-in ks val))

(defn fetch-recipe [id]
  (GET (str "/recipes/" id)
       {:response-format (edn-response-format)
        :handler         #(reset! recipe %)}))

(defn wrap-fetch-recipe [f]
  (letfn [(get-id [this] (-> this r/props :parameters :path :id))]
    (r/create-class
      {:component-did-mount    (fn [this] (fetch-recipe (get-id this)))
       :component-will-unmount (fn [this] (reset! recipe nil))
       :component-did-update   (fn [this] (when-not (= (get-id this)
                                                       (:crux.db/id @recipe))
                                            (fetch-recipe (get-id this))))
       :reagent-render         f})))

(defn remove-incomplete-ingredients [ingredients]
  (vec
    (filter
      (fn [{:ingredient/keys [unit name amount]}]
        (and name (or amount (and unit amount))))
      ingredients)))

(defn save-recipe [evt]
  (.preventDefault evt)
  (PUT "/recipes" {:params  (update @recipe :recipe/ingredients remove-incomplete-ingredients)
                   :format  (edn-request-format)
                   :handler #(rfe/push-state
                               :recipe
                               {:id (:crux.db/id @recipe)})}))

(defn input-unit [idx ing]
  [:select {:value ((fnil name "") (:ingredient/unit ing))
            :on-change
            #(assoc-in-recipe!
               [:recipe/ingredients idx :ingredient/unit]
               (-> % .-target .-value keyword))}
   [:option {:value ""}]
   (for [[u t] [[:ml "ml"] [:dl "dl"] [:l "l"] [:g "g"] [:kg "kg"]
                [:tsp "ts"] [:tbsp "ss"] [:clove "fedd"]]]
     ^{:key u}
     [:option {:value (name u)} t])])

(defn input-float-handler [idx ing]
  (fn [evt]
    (let [nr-str    (-> evt .-target .-value)
          nr-str-us (str/replace nr-str "," ".")
          valid?    (re-matches #"[0-9]*\.?[0-9]*" nr-str-us)
          nr        (js/parseFloat nr-str-us)]
      (when valid?
        (if (js/isNaN nr)
          (update-in-recipe! [:recipe/ingredients idx] dissoc :ingredient/amount)
          (assoc-in-recipe! [:recipe/ingredients idx :ingredient/amount] nr))
        (assoc-in-recipe!
          [:recipe/ingredients idx :ingredient/amount__format] nr-str)))))

(defn input-float [idx ing]
  [:input {:type      "text"
           :on-change (input-float-handler idx ing)
           :value     (or
                        (:ingredient/amount__format ing)
                        (:ingredient/amount ing))}])

(defn input-ingredient-name [idx ing]
  [:input {:type      "text"
           :value     (:ingredient/name ing)
           :on-change #(assoc-in-recipe!
                         [:recipe/ingredients idx :ingredient/name]
                         (-> % .-target .-value))}])

(defn vec-del [coll idx]
  (into (subvec coll 0 idx) (subvec coll (inc idx))))

(defn vec-up [coll idx]
  (if (zero? idx)
    coll
    (into (subvec coll 0 (dec idx))
          (concat
            [(nth coll idx)]
            [(nth coll (dec idx))]
            (subvec coll (inc idx))))))

(defn vec-down [coll idx]
  (if (= (count coll) (inc idx))
    coll
    (into (subvec coll 0 idx)
          (concat
            [(nth coll (inc idx))]
            [(nth coll idx)]
            (subvec coll (+ 2 idx))))))

(defn input-ingredients []
  [:<>
   [:label "Ingredienser"]
   [:ul
    (let [ingredients (:recipe/ingredients @recipe)
          ingredients (if (empty? ingredients) [{}] ingredients)]
      (for [[idx ing] (zipmap (range) ingredients)]
        ^{:key idx}
        [:li
         [input-float idx ing]
         (gstring/unescapeEntities "&nbsp;")
         [input-unit idx ing]
         (gstring/unescapeEntities "&nbsp;")
         [input-ingredient-name idx ing]
         (gstring/unescapeEntities "&nbsp;")
         (gstring/unescapeEntities "&nbsp;")
         [:button {:on-click
                   #(update-recipe! :recipe/ingredients vec-up idx)} "↑"]
         (gstring/unescapeEntities "&nbsp;")
         [:button {:on-click
                   #(update-recipe! :recipe/ingredients vec-down idx)} "↓"]
         (gstring/unescapeEntities "&nbsp;")
         [:button {:on-click
                   #(update-recipe! :recipe/ingredients vec-del idx)} "-"]
         (gstring/unescapeEntities "&nbsp;")
         (when (= (inc idx) (count ingredients))
           [:button
            {:on-click #(update-recipe! :recipe/ingredients (fnil conj []) {})}
            "+"])
         ]))]])

(defn edit-recipe-page [_]
  (wrap-fetch-recipe
    (fn [{{{id :id} :path} :parameters}]
      (when @recipe
        [:form
         [:h1 "Endrer på " (:recipe/name @recipe)]
         [input-text-with-label recipe :recipe/name "Navn"]
         [textarea-with-label recipe :recipe/description "Beskrivelse"]
         [input-int-with-label recipe :recipe/portions "Porsjoner"]
         [input-ingredients]
         [:label "Framangsmåte"]
         [:> ReactQuill
          {:theme     "snow"
           :value     (:recipe/directions @recipe)
           :on-change #(swap! recipe assoc :recipe/directions %)}]
         [:br]
         [:button {:on-click #(rfe/push-state :recipe {:id id})} "Avbryt"]
         (gstring/unescapeEntities "&nbsp;")
         [:button {:type     "submit"
                   :on-click save-recipe} "Lagre"]]))))

(defn recipe-page [_]
  (wrap-fetch-recipe
    (fn [{{{id :id} :path} :parameters}]
      (when @recipe
        [:<>
         [:h1 (:recipe/name @recipe)]
         [:blockquote (:recipe/description @recipe)]
         [:div
          [:h3 "Ingredienser"]
          [:h6 "Porsjoner: " (:recipe/portions @recipe)]
          [:ul
           (for [[idx ing] (zipmap (range) (:recipe/ingredients @recipe))
                 :let      [amount (:ingredient/amount ing)
                            unit (:ingredient/unit ing)
                            name (:ingredient/name ing)]]
             ^{:key idx}
             [:li amount " " unit " " name])]
          [:div
           [:h3 "Framgangsmåte"]
           [:p {:dangerouslySetInnerHTML {:__html (:recipe/directions @recipe)}}]]]
         [:a {:href (rfe/href :edit-recipe {:id id})}
          "Endre på oppskrift"]]))))

