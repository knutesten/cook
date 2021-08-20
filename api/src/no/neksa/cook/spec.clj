(ns no.neksa.cook.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def :crux.db/id uuid?)

(s/def :ingredient/name string?)
(s/def :ingredient/unit #{:ml :dl :l :g :kg :ts :ss :clove})
(s/def :ingredient/amount double?)
(s/def :ingredient/ingredient (s/and (s/keys :req [:ingredient/name]
                                             :opt [:ingredient/unit
                                                   :ingredient/amount])
                                     #(let [{:keys [unit amount]} %]
                                        (not (and unit (not amount))))))
(s/def :recipe/updated-inst inst?)
(s/def :recipe/created-inst inst?)
(s/def :recipe/name string?)
(s/def :recipe/description string?)
(s/def :recipe/portions integer?)
(s/def :recipe/directions string?)
(s/def :recipe/ingredients (s/coll-of :ingredient/ingredient :kind vector? :into []))

(s/def :recipe/recipe-new (s/keys :req [:recipe/name]
                                  :opt [:recipe/description
                                        :recipe/portions
                                        :recipe/ingredients
                                        :recipe/updated-inst
                                        :recipe/created-inst]))
(s/def :recipe/recipe (s/merge (s/keys :req [:crux.db/id])
                               :recipe/recipe-new))

