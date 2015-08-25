(ns accrue.insights.almanac.screener
  (:require [centipair.core.ui :as ui]
            [centipair.core.components.input :as input]
            [reagent.core :as reagent]))


(def pattern-day (reagent/atom {:id "pattern-day" :label "Day" :type "text"}))
(def pattern-month (reagent/atom {:id "pattern-month" :label "Month" :type "text"}))


(defn screener-ui
  []
  [:div {:class "row"}
   [:div {:class "col-md-4"}
    [:form {:class "form-horizontal"}
     (input/text pattern-day)
     (input/text pattern-month)
     
     ]
    ]
   ]
  )


(defn render-screener-ui
  []
  (ui/render screener-ui "content"))
