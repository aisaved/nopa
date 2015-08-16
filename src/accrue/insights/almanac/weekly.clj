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

;;-----------------------------------------------------------------------------


(defn collect-mw-years-data
  [previous next]
  (assoc previous :gl-percent (conj (:gl-percent previous) (:gl-percent next))))

(defn reduce-mw-data
  [each-data]
  (let [data-group (second each-data)
        month (first each-data)
        symbol (:symbol (first data-group))
        init-structure {:gl-percent [] :symbol symbol :month month}]
    (reduce collect-mw-years-data init-structure data-group)))

(defn aggregate-mw-data
  [data]
  (map reduce-mw-data data))

(defn sort-mw-data [each-data]
  [(first each-data) (sort-by #(:sort-key %) (second each-data))])


(defn sorted-mw-data
  [data]
  (map sort-mw-data data))

(defn regroup-mw-data
  [data]
  (group-by #(:month %) data))

(defn fetch-relevant-data
  [each-mw-week-group]
  (calc/ohlc-group-gl-percent each-mw-week-group))

(defn relevant-data
  [each-data]
  (let [mw-week-group (second each-data)
        reference (first (first (second each-data)))
        years-passed (calc/years-passed reference)
        month (t/timestamp-to-month (:time reference))]
    {:symbol (:symbol reference)
     :gl-percent (map fetch-relevant-data mw-week-group)
     :month month
     :years-passed years-passed
     :sort-key (Integer. (str month years-passed))}))

(defn prepare-mw-data [data]
  (map relevant-data data))


(defn mw-week-groups
  [each-data]
  (let [month-group (second each-data)]
    [(first each-data) (conj (into [] (partition 5 month-group )) (take-last 5 month-group))]))

(defn group-mw-week-data
  [data]
  (map mw-week-groups data))


(defn group-mw-data
  [data]
  (group-by #(str (t/timestamp-to-month (:time %)) "-" (calc/years-passed %)) data))


(def transform-mw-data
  (comp
   
   aggregate-mw-data
   sorted-mw-data
   regroup-mw-data
   prepare-mw-data
   group-mw-week-data
   group-mw-data))


;;mw = month weekly pattern
(defn process-mw-pattern
  [symbol]
  (timbre/info (str "Starting month weekly pattern processing for " symbol))
  (let [data (sort-by #(:time %) (data-model/get-history-data symbol "daily"))]
    (transform-mw-data data)))
