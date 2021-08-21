(ns no.neksa.cook.form-util)

(defn input-int-with-label [data key label]
  [:label
   label
   [:br]
   [:input {:type      "text"
            :value     (key  @data)
            :on-change #(let [nr-str (-> % .-target .-value)
                              nr     (js/parseInt nr-str)]
                          (cond
                            (empty? nr-str)           (swap! data dissoc key)
                            (and (int? nr) (pos? nr)) (swap! data assoc key nr)))}]])

(defn input-text-with-label [data key label]
  [:label
   label
   [:br]
   [:input {:type      "text"
            :value     (key  @data)
            :on-change #(swap! data assoc key (-> % .-target .-value))}]])

(defn textarea-with-label [data key label]
  [:label
   label
   [:br]
   [:textarea {:on-change #(swap! data assoc key (-> % .-target .-value))
               :value     (key  @data)}]])
