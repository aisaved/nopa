(ns accrue.insights.almanac.process
  (:require [accrue.data.models :as data-model]
            [accrue.utilities.time :as t]
            [accrue.insights.almanac.calendar :as cal]
            [accrue.data.barchart :as barchart]
            [accrue.math.stats :as stats]
            [accrue.data.symbols :as symbols]
            [accrue.insights.almanac.models :as almanac-models]
            ))

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
  [data-group]
  (- pattern-current-year (t/timestamp-to-year (:time (first data-group)))))

;;Data transformations for partitioned data
(defn n-pattern
  [n list-data]
  (partition n 1 list-data))


(defn data-format
  "Only first and last of data group is relevant
  TODO: This a stict rule. Probably will have to find 
  a relevant profit in next n-days"
  [data-group]
  {:gl-percent (gl-percent (:open (first data-group)) (:close (last data-group)))
   :years-passed (years-passed data-group)
   :symbol (:symbol (first data-group))
   :day (t/timestamp-to-day (:time (first data-group)))
   :month (t/timestamp-to-month (:time (first data-group)))
   :pattern-length (count data-group)
   ;;:raw-data data-group ;;TODO pass 
   })

(defn prepare-data
  [data]
  (let [pattern-range (range 5 101)]
    (for [pattern-length pattern-range]
      (let [parted-data (n-pattern pattern-length data)]
        (map data-format parted-data)))))


;;-----------------Yearly gl-percent collectors------------
(defn generate-years-passed-array
  [next]
  (keep-indexed (fn [index each]
                  (if (= index (:years-passed next))
                    (:gl-percent next)
                    0))
                (range 5 101)))


(defn collect-years-passed-data
  [key ag-data next]
  (let [ag-data-row (key ag-data)]
    (assoc (:gl-data ag-data-row) (:years-passed next) (:gl-percent next))))


(defn collect-gl-data
  [key ag-data next]
  (if (nil? (key ag-data))
    (into [] (generate-years-passed-array next))
    (collect-years-passed-data key ag-data next)))
;;-----------------Yearly gl-percent collectors------------

;;---------------Raw data collectors----------------------
(defn generate-years-passed-raw-data-array
  [next]
  (keep-indexed (fn [index each]
                  (if (= index (:years-passed next))
                    (:raw-data next)
                    []))
                (range 5 101)))

(defn collect-years-passed-raw-data
  [key ag-data next]
  (let [ag-data-row (key ag-data)]
    (assoc (:raw-data ag-data-row) (:years-passed next) (:raw-data next))))


(defn collect-raw-data
  [key ag-data next]
  (if (nil? (key ag-data))
    (into [] (generate-years-passed-raw-data-array next))
    (collect-years-passed-raw-data key ag-data next)))
;;---------------Raw data collectors------------------


(defn reduce-n-day-pattern
  [ag-data next]
  (let [key (keyword (str (:day next) "-" (:month next)))]
    (assoc ag-data
           key {:pattern-length (:pattern-length next)
                :symbol (:symbol next)
                :gl-data (collect-gl-data key ag-data next)
                :raw-data (collect-raw-data key ag-data next)})))


(defn n-day-pattern-aggregate
  [n-day-data-row]
  (reduce reduce-n-day-pattern {} n-day-data-row))


(defn n-day-aggregate
  [data]
  (map n-day-pattern-aggregate data))



(defn find-yearly-sd
  [percent-gain-years]
  (map #(stats/standard-deviation (take % percent-gain-years))
       (range 5 (count percent-gain-years))))

(defn find-yearly-win-percent
  [percent-gain-years]
  (map #(stats/win-percent (take % percent-gain-years))
       (range 5 (count percent-gain-years))))


(defn find-yearly-average-gl-percent
  [percent-gain-years]
  (map #(stats/average (take % percent-gain-years))
       (range 5 (count percent-gain-years))))


(defn find-next-day-value
  "Find next day value for a day
  Tries for next five days."
  [data day-key year-index tries]
  (if (> tries 4)
    0
    (let [next-day-key (cal/find-next-day-key day-key)
          next-day-value (nth (:gl-data (next-day-key data)) year-index)]
      (if (= 0 next-day-value)
        (find-next-day-value data next-day-key year-index (inc tries))
        next-day-value))))


(defn fill-data
  "Fills in holiday data"
  [data day-key year-index gl]
  (if (= gl 0)
    (find-next-day-value data day-key year-index 0)
    gl))



(defn find-next-day-raw-data
  "Find next day raw data for a day
  Tries for next five days."
  [data day-key year-index tries]
  (if (> tries 4)
    []
    (let [next-day-key (cal/find-next-day-key day-key)
          next-day-value (nth (:raw-data (next-day-key data)) year-index)]
      (if (empty? next-day-value)
        (find-next-day-raw-data data next-day-key year-index (inc tries))
        next-day-value))))


(defn fill-raw-data
  [data day-key year-index raw-data]
  (if (empty? raw-data)
    (find-next-day-raw-data data day-key year-index 0)
    raw-data))
 

(defn process-data
  "Checks for blank data and creates new map from filled data"
  [data new-n-day-data key-group]
  (let [day-key (first key-group)
        past-years-data-list (:gl-data (second key-group))
        past-years-raw-data-list (:raw-data (second key-group))
        filled-data-array (filter
                           #(not (= % 0))
                           (keep-indexed (partial fill-data data day-key) past-years-data-list))
        filled-raw-data-array (filter
                               #(not (empty? %))
                               (keep-indexed (partial fill-raw-data data day-key) past-years-raw-data-list))
        filled-data (assoc (day-key data)
                           :gl-data-filled filled-data-array
                           ;;:raw-data-filled filled-raw-data-array ;; TODO - save raw data
                           ;;:avg-gl (stats/average filled-data-array) ;; TODO - is this required?
                           :avg-gl-percent (find-yearly-average-gl-percent filled-data-array)
                           :sd (find-yearly-sd filled-data-array)
                           :win-percent (find-yearly-win-percent filled-data-array))
        filled-n-day-data (assoc new-n-day-data day-key filled-data)]
    (almanac-models/save-data day-key filled-data)
    filled-n-day-data))


(defn process-n-day-data
  "Process each n-day blanks"
  [n-day-data]
  (reduce (partial process-data n-day-data)  {} n-day-data))


(defn compute-save
  "If a particular day is a holiday, fetch data from the next trading day"
  [data]
  (doall (map process-n-day-data data)))


;; This function composition detects patterns and
;; transforms raw ohlc data into relevant pattern data
(def transform-data
  (comp
   compute-save
   n-day-aggregate
   prepare-data))


;;Data transformations for partitioned data ends


(defn process-daily-symbol
  "Processing each symbol"
  [symbol]
  (let [data (sort-by #(:time %) (data-model/get-history-data symbol "daily"))]
    (transform-data data)))



(defn process-all-symbols
  []
  (pmap process-daily-symbol ["MCR" "NFLX" "AAPL" "IBM" "MSFT" "FB"]))
