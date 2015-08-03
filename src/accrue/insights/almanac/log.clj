(ns accrue.insights.almanac.log
  (:use clojurewerkz.cassaforte.query)
  (:require [accrue.utilities.time :as time]
            [accrue.insights.almanac.models :as almanac-model]
            [clojurewerkz.cassaforte.cql :as cql]
            [centipair.core.db.connection :as conn]))



(defn daily-data-available?
  [symbol]
  (let [info (first (cql/select (conn/dbcon)
                          almanac-model/almanac-log-table
                          (where [[= :symbol symbol] [= :process_type "daily"]])))]
    (if (empty? info)
      false
      (:data_fetched info))))


(defn daily-data-processed?
  [symbol]
  (let [info (first (cql/select (conn/dbcon)
                          almanac-model/almanac-log-table
                          (where [[= :symbol symbol] [= :process_type "daily"]])))]
    (if (empty? info)
      false
      (:data_processed info))))


(defn daily-data-fetched
  [symbol]
  (cql/insert (conn/dbcon)
              almanac-model/almanac-log-table
              {:symbol symbol
               :process_type "daily"
               :data_fetched true
               :data_processed false}))


(defn daily-data-processed
  [symbol]
  (cql/insert (conn/dbcon)
              almanac-model/almanac-log-table
              {:symbol symbol
               :process_type "daily"
               :data_fetched true
               :data_processed true}))
