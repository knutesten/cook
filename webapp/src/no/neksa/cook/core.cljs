(ns no.neksa.cook.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [no.neksa.cook.ajax]
   [no.neksa.cook.routes :refer [start-router!]]
   [no.neksa.cook.layout :refer [layout-page]]))

(defn mount-root []
  (d/render [layout-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (start-router!)
  (mount-root))

