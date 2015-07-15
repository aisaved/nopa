(ns accrue.data.barchart
  (:use centipair.core.db.connection
        accrue.db.models
        accrue.util)
  (:require [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clj-time.coerce :as c]
            [accrue.data.symbols :as symbols]))


(def barchart-ohlc-table "barchart_ohlc")

(def barchart-api-key "026368620175b407e3a5907138ffe427")
(def barchart-url "http://ondemand.websol.barchart.com/getHistory.json")

(defn barchart-accrue-ohlc
  "barchart ohlc to accrue ohlc data mapping"
  [barchart-ohlc params]
  (let [standard-interval ((keyword (:type params)) intervals)
        key-maker {:symbol (:symbol params)
                   :interval standard-interval
                   :year (timestamp-to-year (c/to-sql-time (:timestamp barchart-ohlc)))
                   }]
    {:ohlc_id (partition-key key-maker)
     :symbol (:symbol params)
     :open (:open barchart-ohlc)
     :high (:high barchart-ohlc)
     :low (:low barchart-ohlc)
     :close (:close barchart-ohlc)
     :volume (Integer. (:volume barchart-ohlc))
     :time (c/to-sql-time (:timestamp barchart-ohlc))
     :interval standard-interval
     }
    ))

(defn cql-insert-ohlc
  "Saves barchart ohlc to accrue db"
  [barchart-ohlc-results params]
  (doseq [barchart-ohlc barchart-ohlc-results]
    (cql/insert conn barchart-ohlc-table (barchart-accrue-ohlc barchart-ohlc params))))


(defn get-options
  "Adds extra options to the http request"
  [params]
  {:query-params (merge {:apikey barchart-api-key} params)})

(defn fetch-save-ohlc
  "Params (map):symbol, interval, type, maxRecords, order
  For daily data of apple for past 1000 days {:symbol \"AAPL\" :interval 1 :type \"daily\" :maxRecords 1000 :order \"desc\"}"
  [params]
  (let [options (get-options params)
        api-response (http/get  barchart-url options)]
    (if (= (:status @api-response) 200)
      (cql-insert-ohlc (:results (json/read-json (:body @api-response))) params)
      (println (str "Something wrong-> status code: " (:status @api-response))))))

(defn select-barchart-data [partition-keys]
  (cql/select conn barchart-ohlc-table (query/where 
                          :ohlc_id [:in partition-keys])))


(defn start-barchart-scheduling
  ;;TODO: add the scheduler here
  []
  (let [all-symbols (symbols/select-all-symbols)]
    (count all-symbols)))
