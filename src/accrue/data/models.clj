(ns accrue.data.models
  (:use clojurewerkz.cassaforte.query
        clojurewerkz.cassaforte.uuids
        [cheshire.core :refer :all])
  (:require [clojurewerkz.cassaforte.cql :as cql]
            [centipair.core.db.connection :as conn]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))


(def data-source {:quantumcharts {:symbols "quantum_charts_symbols"
                                  :ohlc "quantum_charts_ohlc"}
                  :barchart {:symbols "barchart_symbols"
                             :ohlc "barchart_ohlc"}
                  :iqfeed {:symbols "iqfeed_symbols"
                           :ohlc "iqfeed_ohlc"}})
(def ohlc-table "ohlc")

(def intervals {:seconds 1
                :minutes 60 ;;sixty seconds
                :hourly (* 60 60)
                :daily (* 24 60 60)
                :weekly (* 7 24 60 60)
                :monthly (* 31 24 60 60)})


(defn timestamp-to-year
  "Returns the year from the given timestamp"
  [timestamp]
  (let [datetime (c/from-sql-date timestamp)]
    (t/year datetime)))

(defn date-to-year
  "Returns the year from the given datetime value"
  [date]
  (t/year date))


(defn partition-key 
  " Generates the partition key for the single ohlc data
    Partition key is of the format symbol-interval-year
    ohlc point Eg: {:year <year>(a number) :symbol 'AAPL' :interval 5}"
  [ohlc-point]
  (str (:symbol ohlc-point) "-"
       (:interval ohlc-point) "-"
       (:year ohlc-point)))

(defn get-symbols
  "Get symbols from the given data source"
  [source]
  (map #(% :symbol) (cql/select (conn/dbcon) (:symbols ((keyword source) data-source)))))


(defn highchart-ohlc-format 
  [ohlc]
  [(c/to-long (:time ohlc)) (:open ohlc) (:high ohlc) (:low ohlc) (:close ohlc)])


(defn accrue-ohlc-format
  [ohlc]
  {:time (c/to-long (:time ohlc)) :open (:open ohlc) :high (:high ohlc) :low (:low ohlc) :close (:close ohlc)})

(defn highcharts-ohlc-js
  "converts from db format to highcharts data format"
  [results]
  (into [] (map highchart-ohlc-format results)))

(defn get-ohlc
  "Partition keys are of format: symbol-intervalseconds-year
  EG: AAPL-86400-2015
  start-date/end-date standard date format"
  [partition-keys start-date end-date]
  (cql/select (conn/dbcon) ohlc-table
              (where
               :ohlc_id [:in partition-keys]
               :time [> (c/to-sql-time start-date)]
               :time [<= (c/to-sql-time end-date)])))


