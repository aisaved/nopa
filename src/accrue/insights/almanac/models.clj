(ns accrue.insights.almanac.models
  (:require [clojurewerkz.cassaforte.cql    :as cql]
            [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]
            [accrue.data.models :as data-model]
            [accrue.utilities.time :as t]))



(defn date-id
  "converts date to date-id format
  used as primary key in almanac table
  date-id format:<day>-<month>-<past years>
  Example : 1-07-25"
  [date past-years]
  (str (t/date-to-day date) "-" (t/date-to-month date) "-" past-years))

;;Data transformations for partitioned data
(defn relevant-data
  "Only first and last of data group is relevant"
  [data-group]
  {:gl-percent nil
   :sd nil
   :win-percent nil
   :data [(first data-group) (last data-group)]})



(def transform-daily-data
  (comp
   
   prepare-data
   ))
;;Data transformations for partitioned data ends




(defn n-pattern
  [n list-data]
  (partition n 1 list-data))



(defn get-history-data
  [symbol interval]
  (cql/select (conn/dbcon)
              data-model/ohlc-table
              (where [[:in :ohlc_id (data-model/past-years-key symbol interval 100)]])))


(defn process-daily-symbol
  [symbol]
  (let [data (sort-by #(:time %) (get-history-data symbol "daily"))]
    (doseq [pattern-length (range 5 6)]
      (let [parted-data (n-pattern pattern-length data)]
        (println (last parted-data))
        ))))
