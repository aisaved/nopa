(ns accrue.insights.almanac.imp
  (:require [accrue.data.models :as data-model]
            [accrue.utilities.time :as t]
            [accrue.insights.almanac.calendar :as cal]
            [accrue.data.barchart :as barchart]
            [accrue.math.stats :as stats]
            [accrue.data.symbols :as symbols]
            [accrue.insights.almanac.models :as almanac-models]
            [taoensso.timbre :as timbre]))


(defn fetch-test-data
  "Fetches test data using barchart api"
  [symbol]
  (barchart/fetch-save-ohlc {:symbol symbol
                             :interval 1
                             :type "daily"
                             :maxRecords 20000
                             :order "desc"}))

(def pattern-current-year (t/current-year))




(defn gl-percent
  "Calculates gain lose percentage"
  [open close]
  (with-precision 5
    (* (/ (- close open) open) 100)))


(defn years-passed
  [key-data]
  (- pattern-current-year (t/timestamp-to-year (:time key-data))))







(defn partition-data
  [data]
  (partition 100 1 data))



(defn gl-percent-n-day
  [key-data index n-data]
  (gl-percent (:open key-data) (:close n-data)))


(defn prepare-data-row
  [data-row]
  (let [key-data (first data-row)]
    {:years-passed (years-passed key-data)
     :symbol (:symbol key-data)
     :day (t/timestamp-to-day (:time key-data))
     :month (t/timestamp-to-month (:time key-data))
     :gl-percent (keep-indexed (partial gl-percent-n-day key-data) data-row)
     }))

(defn prepare-data
  [data]
  (map prepare-data-row data))


(def transform-data
  (comp
   prepare-data
   partition-data))




(defn process-daily-symbol
  "Processing each symbol"
  [symbol]
  (timbre/info (str "Starting daily pattern processing for " symbol))
  (let [data (sort-by #(:time %) (data-model/get-history-data symbol "daily"))]
    (transform-data data)
    ;;(timbre/info (str "Finished Daily pattern processing for " symbol " ****** "))
    ))
