(ns no.neksa.cook.spec.util
  (:require
   [clojure.spec.alpha :as s]
   [clojure.set :as set]))

(defn- unqualify-keywords
  "Takes a collection of qualified keywords and returns a sequence of simple
  keywords.

  Ex. (:user/test :user/test2) -> (:test :test2)"
  [keywords]
  (map #(keyword (name %)) keywords))

(defn- parse-form-for-keys
  "Takes a form and returns a touple of new forms to parse and keys from
  any clojure.spec.alpha/keys form."
  [[form-name & params]]
  (case form-name
    clojure.spec.alpha/keys
    (let [{:keys [req opt req-un opt-un]} params
          req-un                          (unqualify-keywords req-un)
          opt-un                          (unqualify-keywords opt-un)]
      [[] (concat req opt req-un opt-un)])

    clojure.spec.alpha/merge
    [params []]))

(defn- spec-keys
  "Returns the keys that are defined as part of a spec. Both required and
  optinal keys are returned.

  Ex.
  (do
    (s/def ::test (s/keys :req [::a] :opt-un [::b]))
    (spec-keys ::test))

  -> (:user/a :b)"
  ([x] (spec-keys [x] []))
  ([[x & xs] ks]
   (cond
     (keyword x) (recur (conj xs (s/get-spec x)) ks)
     (s/spec? x) (recur (conj xs (s/form x)) ks)
     (seq? x)    (let [[new-xs new-ks] (parse-form-for-keys x)]
                   (recur (concat new-xs xs) (concat new-ks ks)))
     :else       ks)))

(defn valid-closed?
  "Validates that x conforms to the given spec like s/valid?, but also validates
  that no extra keywords exists other than those specified in the spec.

  Ex.
  (s/def ::a string?)
  (s/def ::test (s/keys :req [::a]))

  (s/valid? ::test {:a \"test\" :b 3})
  -> true

  (s/valid-closed? ::test {:a \"test\" :b 3})
  -> false"
  [spec x]
  (let [spec-ks (set (spec-keys spec))
        x-ks    (set (keys x))]
    (and (set/subset? x-ks spec-ks)
         (s/valid? spec x))))

