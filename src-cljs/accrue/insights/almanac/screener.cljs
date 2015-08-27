(ns accrue.insights.almanac.screener
  (:require [centipair.core.ui :as ui]
            [centipair.core.components.input :as input]
            [reagent.core :as reagent]))


(def gl-percent-range (doall (map (fn [each] {:value each :label (str each)}) (range -100 101))))
(def accuracy-range (doall (map (fn [each] {:value each :label (str each)}) (range 70 101))))
(def pattern-length-days (doall (map (fn [each] {:value each :label (str each " days")}) (range 2 101))))
(def history-range-years (doall (map (fn [each] {:value each :label (str each " years")}) (range 5 101))))
(def sd-range (doall (map (fn [each] {:value each :label (str "< " each "%")}) (range 1 101))))

(def pattern-day (reagent/atom {:id "pattern-day" :label "Day" :type "text"}))
(def pattern-month (reagent/atom {:id "pattern-month" :label "Month" :type "text"}))
 
(def gl-range (reagent/atom {:id "gl-range" :label "Average G/L % Range" :type "select-range"
                             :from {:id "gl-range-start" :options gl-percent-range}
                             :to {:id "gl-range-end" :options gl-percent-range}}))
(def accuracy (reagent/atom {:id "accuracy-range" :label "Pattern Accuracy % Range" :type "select-range"
                             :from {:id "gl-range-start" :options accuracy-range}
                             :to {:id "gl-range-end" :options accuracy-range}}))

(def pattern-length (reagent/atom {:id "pattern-length" :type "select" :options pattern-length-days :label "Pattern length"}))

(def history-years (reagent/atom {:id "history-years" :label "Pattern History Range" :type "select-range"
                                  :from {:id "history-range-start" :options history-range-years}
                                  :to {:id "history-range-end" :options history-range-years}}))

(def sd (reagent/atom {:id "sd" :type "select" :options sd-range :label "Standard deviation"}))


(defn daily-screener-ui
  []
  [:div {:class "row"}
   [:div {:class "col-md-6"}
    [:form {:class "form-horizontal"}
     (input/text pattern-month)
     (input/text pattern-day)
     (input/select-range gl-range)
     (input/select-range accuracy)]]
   [:div {:class "col-md-6"}
    [:form {:class "form-horizontal"}
     (input/select pattern-length)
     (input/select-range history-years)
     (input/select sd)]]])


(defn render-screener-ui
  []
  (ui/render daily-screener-ui "content"))
