(ns no.neksa.cook.layout
  (:require
   [reitit.frontend.easy :as rfe]
   [no.neksa.cook.routes :refer [routes route]]))

(defn nav-link [route-name link-text current-route]
  [:a {:href (rfe/href route-name)}
   (if (= route-name (-> current-route :data :name))
     [:b link-text]
     link-text)])

(defn navbar [current-route]
  [:nav
   (interpose
     ^{:key "|"}[:span " | "]
     (for [[_ r] routes
           :let  [route-name (:name r)
                  link-text (:label r)]
           :when link-text]
       ^{:key route-name}
       [nav-link route-name link-text current-route]))])

(defn layout-page []
  (let [current-route @route
        view          (-> current-route :data :view)]
    (when view
      [:<>
       [navbar current-route]
       [:section
        [view current-route]]])))
