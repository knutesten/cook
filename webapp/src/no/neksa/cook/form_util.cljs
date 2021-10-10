(ns no.neksa.cook.form-util)

(defn input-text-with-label [value label on-change]
  [:label
   label
   [:br]
   [:input {:type      "text"
            :value     value
            :on-change #(on-change (.. % -target -value))}]])

(defn textarea-with-label [value label on-change]
  [:label
   label
   [:br]
   [:textarea {:on-change #(on-change (.. % -target -value))
               :value     value}]])

