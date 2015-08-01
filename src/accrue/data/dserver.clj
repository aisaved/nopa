(ns accrue.data.dserver
  (:require [org.httpkit.client :as http]
             [cheshire.core :refer [parse-string]]
             [clj-http.client :as client]
             [accrue.data.models :as data-model]
             [accrue.utilities.time :as t]
             [accrue.data.symbols :as symbols]
            ))


(def dserver-history-url "http://login.accrue.com/api/history")




(defn dserver-accrue-ohlc
  "barchart ohlc to accrue ohlc data mapping"
  [params result-vector]
  (let [standard-interval ((keyword (:type params)) data-model/intervals)
        key-maker {:symbol (:symbol params)
                   :interval standard-interval
                   :year (t/timestamp-to-year (t/to-sql-time (nth result-vector 5)))
                   }]
    {:ohlc_id (data-model/partition-key key-maker)
     :symbol (:symbol params)
     :open (nth result-vector 0)
     :high (nth result-vector 2)
     :low (nth result-vector 3)
     :close (nth result-vector 6)
     :volume (Integer. (nth result-vector 1))
     :time (t/to-sql-time (nth result-vector 5))
     :interval standard-interval
     }))


(defn fetch-daily-data
  [symbol-map]
  (println (str "fetching---" (:symbol symbol-map)))
  (let [symbol (:symbol symbol-map)
        query-params {:symbol symbol
                      :barsize 86400
                      :barcount 9341
                      :type "daily"
                      }
        results (client/get dserver-history-url {:accept :json
                                     :query-params query-params})
        result-vector (rest (get-in (parse-string (:body results)) ["data" symbol]))]
    
    (doseq [accrue-ohlc (map (partial dserver-accrue-ohlc query-params) result-vector)]
      (data-model/save-ohlc accrue-ohlc))))


(defn start-dserver-scheduling
  ;;TODO: add the scheduler here
  []
  (let [all-symbols (symbols/select-all-symbols)
        symbol-grouped (partition-all 20 all-symbols)]
    (doseq [symbols symbol-grouped] (pmap fetch-daily-data symbols))
    ;;(doseq [symbol all-symbols] (fetch-daily-data symbol))
    ))

