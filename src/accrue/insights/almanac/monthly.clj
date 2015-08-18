(ns accrue.insights.almanac.monthly
  (:require [accrue.data.models :as data-model]
            [accrue.utilities.time :as t]
            [accrue.insights.almanac.calendar :as cal]
            [accrue.insights.almanac.calc :as calc]
            [accrue.data.barchart :as barchart]
            [accrue.math.stats :as stats]
            [accrue.data.symbols :as symbols]
            [accrue.insights.almanac.models :as almanac-models]
            [taoensso.timbre :as timbre]))



(defn fetch-test-monthly-data
  "Fetches test data using barchart api"
  [symbol]
  (barchart/fetch-save-ohlc {:symbol symbol
                             :interval 1
                             :type "daily"
                             :maxRecords 20000
                             :order "desc"}))





(defn group-monthly-data
  [data]
  (group-by #(str (t/timestamp-to-month (:time %)) "-" (t/timestamp-to-year (:time %))) data))


(defn relevant-data
  [each]
  (let [reference (first (second each))
        month (t/timestamp-to-month (:time reference))
        years-passed (calc/years-passed reference)]
    {:symbol (:symbol reference)
     :gl-percent (calc/ohlc-group-gl-percent (second each))
     :month month
     :years-passed years-passed
     :sort-key (Integer. (str month years-passed))}))

(defn relevant-data-group
  [data]
  (map relevant-data data))


(defn regroup-monthly-data
  [data]
  (group-by #(:month %) data))



(defn sort-regrouped-monthly-data
  [each-data]
  (let [symbol (:symbol (first (second each-data)))
        sorted-data-map (sort-by #(:sort-key %) (second each-data))
        sorted-gl-percent (map #(:gl-percent %) sorted-data-map)
        ]
    
    {:month (first each-data) :symbol symbol :gl-percent sorted-gl-percent}))


(defn arrange-regrouped-data
  [data]
  (map sort-regrouped-monthly-data data))


(defn compute-each
  [each-data]
  (assoc each-data
         :sd (calc/find-yearly-sd (:gl-percent each-data))
         :avg-gl-percent (calc/find-yearly-average-gl-percent (:gl-percent each-data))
         :win-percent (calc/find-yearly-win-percent (:gl-percent each-data))))

(defn compute
  [data]
  (map compute-each data))


(defn save-monthly-data [data]
  (doseq [each-data data]
    (almanac-models/save-monthly-data each-data)))

(def transform-monthly-data
  (comp
   save-monthly-data
   compute
   arrange-regrouped-data
   regroup-monthly-data
   relevant-data-group
   group-monthly-data))


(defn process-monthly-pattern
  [symbol]
  (timbre/info (str "Starting monthly pattern processing for " symbol))
  (let [data (sort-by #(:time %) (data-model/get-history-data symbol "daily"))]
    (transform-monthly-data data)
    (timbre/info (str "Finished Monthly pattern processing for " symbol " ****** "))))
