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



(defn initial-date-group-structure
  [next]
  (let [gl-percent-list (into [] (take 100 (repeat [])))
        gl-percent-list-appended (assoc gl-percent-list (:years-passed next) (:gl-percent next))]
    {:symbol (:symbol next)
     :gl-percent gl-percent-list-appended}))

(defn append-date-group-structure
  [previous-structure next]
  (let [gl-percent-list (:gl-percent previous-structure)
        gl-percent-list-appended (assoc gl-percent-list (:years-passed next) (:gl-percent next))]
    {:symbol (:symbol next)
     :gl-percent gl-percent-list-appended}))

(defn reduce-date
  [previous next]
  (let [date-key (keyword (str (:day next) "-" (:month next)))]
    (if (nil? (date-key previous))
      (assoc previous date-key
             (initial-date-group-structure next))
      (assoc previous date-key (append-date-group-structure (date-key previous) next)))))


(defn arrange-data
  [data]
  (reduce reduce-date {} data)) 



(defn find-next-day-gl-percent
  "Find next day value for a day
  Tries for next five days."
  [data day-key year-index tries]
  (if (> tries 4)
    []
    (let [next-day-key (cal/find-next-day-key day-key)
          next-day-value (nth (:gl-percent (next-day-key data)) year-index)]
      (if (empty? next-day-value)
        (find-next-day-gl-percent data next-day-key year-index (inc tries))
        next-day-value))))



(defn fill-data
  [data date-key year-index yearly-gl-percent]
  (if (empty? yearly-gl-percent)
    (find-next-day-gl-percent data date-key year-index 0)
    yearly-gl-percent))



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


(defn compute-n-day-data
  [data previous next]
  (let [date-key (first next)
        gl-percent-list (:gl-percent (second next))
        filled-holidays-list (keep-indexed (partial fill-data data date-key) gl-percent-list)
        non-empty-gl-percent-list (filter #(not (empty? %)) filled-holidays-list)
        t-data (apply map list non-empty-gl-percent-list)
        sd (map find-yearly-sd t-data)
        win-percent (map find-yearly-win-percent t-data)
        avg-gl-percent (map find-yearly-average-gl-percent t-data)
        save-map {:gl-data-filled t-data
                  :symbol (:symbol (second next))
                  :sd sd
                  :avg-gl-percent avg-gl-percent
                  :win-percent win-percent
                  }
        filled-data (assoc previous date-key
                           save-map)
        ]
    (timbre/info (str "Saving data " (:symbol (second next))) " " date-key)
    (almanac-models/save-group-data date-key save-map)
    ;;filled-data
    
    ))

(defn compute-data
  [data]
  (reduce (partial compute-n-day-data data) {} data))



(def transform-data
  (comp
   compute-data
   arrange-data
   prepare-data
   partition-data))




(defn process-daily-symbol
  "Processing each symbol"
  [symbol]
  (timbre/info (str "Starting new daily pattern processing for " symbol))
  (let [data (sort-by #(:time %) (data-model/get-history-data symbol "daily"))]
    (transform-data data)
    (timbre/info (str "Finished Daily pattern processing for " symbol " ****** "))
    ))
