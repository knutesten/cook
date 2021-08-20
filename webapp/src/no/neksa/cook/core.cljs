(ns no.neksa.cook.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [no.neksa.cook.ajax]
   [no.neksa.cook.routes :refer [start-router!]]
   [no.neksa.cook.layout :refer [layout-page]]))

(defn mount-root []
  (let [app-elem (.getElementById js/document "app")]
    (d/unmount-component-at-node app-elem)
    (d/render [layout-page] app-elem)))

(defn ^:export init! []
  (start-router!)
  (mount-root))

