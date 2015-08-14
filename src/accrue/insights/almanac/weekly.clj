(ns accrue.insights.almanac.weekly
  (:require [accrue.data.models :as data-model]
            [accrue.utilities.time :as t]
            [accrue.insights.almanac.calendar :as cal]
            [accrue.insights.almanac.calc :as calc]
            [accrue.data.barchart :as barchart]
            [accrue.math.stats :as stats]
            [accrue.data.symbols :as symbols]
            [accrue.insights.almanac.models :as almanac-models]
            [taoensso.timbre :as timbre]))







(defn group-weekly-data 
  [data]
  (group-by #(str (t/timestamp-to-week (:time %)) "-" (t/timestamp-to-year (:time %))) data))



(defn relevant-data
  [each]
  (let [reference (first (second each))
        week (t/timestamp-to-week (:time reference))
        years-passed (calc/years-passed reference)]
    {:symbol (:symbol reference)
     :gl-percent (calc/ohlc-group-gl-percent (second each))
     :week week
     :years-passed years-passed
     :sort-key (Integer. (str week years-passed))}))

(defn relevant-data-group
  [data]
  (map relevant-data data))


(defn regroup-weekly-data
  [data]
  (group-by #(:week %) data))



(defn sort-regrouped-weekly-data
  [each-data]
  (let [symbol (:symbol (first (second each-data)))
        sorted-data-map (sort-by #(:sort-key %) (second each-data))
        sorted-gl-percent (map #(:gl-percent %) sorted-data-map)
        ]
    
    {:week (first each-data) :symbol symbol :gl-percent sorted-gl-percent}))


(defn arrange-regrouped-data
  [data]
  (map sort-regrouped-weekly-data data))

(defn compute-each
  [each-data]
  (assoc each-data
         :sd (calc/find-yearly-sd (:gl-percent each-data))
         :avg-gl-percent (calc/find-yearly-average-gl-percent (:gl-percent each-data))
         :win-percent (calc/find-yearly-win-percent (:gl-percent each-data))))

(defn compute
  [data]
  (map compute-each data))


(defn save-weekly-data [data]
  (doseq [each-data data]
    (almanac-models/save-weekly-data each-data)))


(def transform-weekly-data
  (comp
   save-weekly-data
   compute
   arrange-regrouped-data
   regroup-weekly-data
   relevant-data-group
   group-weekly-data))



(defn process-weekly-pattern
  [symbol]
  (timbre/info (str "Starting weekly pattern processing for " symbol))
  (let [data (sort-by #(:time %) (data-model/get-history-data symbol "daily"))]
    (transform-weekly-data data)))
