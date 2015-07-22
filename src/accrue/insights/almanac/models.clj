(ns accrue.insights.almanac.models
  (:require [clojurewerkz.cassaforte.cql    :as cql]
            [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]
            [accrue.data.models :as data-model]
            [accrue.utilities.time :as t]))



(def pattern-current-year (t/current-year))

(defn date-id
  "converts date to date-id format
  used as primary key in almanac table
  date-id format:<day>-<month>-<past years>
  Example : 1-07-25"
  [date past-years]
  (str (t/date-to-day date) "-" (t/date-to-month date) "-" past-years))

(defn gl-percent
  "Calculates gain lose percentage"
  [open close]
  (with-precision 5
    (* (/ (- close open) open) 100)))


(defn years-passed
  [data-group]
  (- pattern-current-year (t/timestamp-to-year (:time (first data-group)))))

;;Data transformations for partitioned data
(defn n-pattern
  [n list-data]
  (partition n 1 list-data))


(defn data-format
  "Only first and last of data group is relevant"
  [data-group]
  {:gl-percent (gl-percent (:open (first data-group)) (:close (last data-group)))
   :years-passed (years-passed data-group)
   :symbol (:symbol (first data-group))
   :day (t/timestamp-to-day (:time (first data-group)))
   :month (t/timestamp-to-month (:time (first data-group)))
   :pattern-length (count data-group)
   :data [(first data-group) (last data-group)]})

(defn prepare-data
  [data]
  (let [pattern-range (range 5 101)]
    (for [pattern-length pattern-range]
      (let [parted-data (n-pattern pattern-length data)]
        (map data-format parted-data)))))



;;{:month 1 :day 1 :years 0 :gl-precent 4.5 :symbol "AAPL"}
;;{:month 1 :day 2 :years 0 :gl-precent 5.5 :symbol "AAPL"}


(defn generate-years-passed-array
  [next]
  (keep-indexed (fn [index each]
                  (if (= index (:years-passed next))
                    (:gl-percent next)
                    0
                    ))(range 5 101)))


(defn collect-years-passed-data
  [key ag-data next]
  (let [ag-data-row (key ag-data)]
    (assoc (:gl-data ag-data-row) (:years-passed next) (:gl-percent next))))


(defn collect-gl-data
  [key ag-data next]
  (if (nil? (key ag-data))
    (into [] (generate-years-passed-array next))
    (collect-years-passed-data key ag-data next)))


(defn reduce-n-day-pattern
  [ag-data next]
  (let [key (keyword (str (:day next) "-" (:month next)))]
    (assoc ag-data
             key {:pattern-length (:pattern-length next)
                  :symbol (:symbol next)
                  :gl-data (collect-gl-data key ag-data next)})))


(defn n-day-pattern-aggregate
  [n-day-data-row]
  (reduce reduce-n-day-pattern {} n-day-data-row))


(defn n-day-aggregate
  [data]
  (map n-day-pattern-aggregate data))


;; This function composition detects patterns and
;; transforms raw ohlc data into relevant pattern data
(def transform-data
  (comp
   n-day-aggregate
   prepare-data))


;;Data transformations for partitioned data ends



(defn get-history-data
  [symbol interval]
  (cql/select (conn/dbcon)
              data-model/ohlc-table
              (where [[:in :ohlc_id (data-model/past-years-key symbol interval 100)]])
              ;;(limit 100)
              ))



(defn process-daily-symbol
  "Processing each symbol"
  [symbol]
  (let [data (sort-by #(:time %) (get-history-data symbol "daily"))]
    (transform-data data)))
