(ns accrue.insights.almanac.log
  (:use clojurewerkz.cassaforte.query)
  (:require [accrue.utilities.time :as time]
            [accrue.insights.almanac.models :as almanac-model]
            [clojurewerkz.cassaforte.cql :as cql]
            [centipair.core.db.connection :as conn]))




(def almanac-log-table "almanac_log")

(defn data-available?
  [interval symbol]
  (let [log-info (first (cql/select (conn/dbcon)
                                    almanac-log-table
                                    (where [[= :process interval] [= :symbol symbol] ])))]
    (if (empty? log-info)
      false
      (:completed log-info))))




(defn data-processed?
  [interval symbol]
  (let [log-info (first (cql/select (conn/dbcon)
                          almanac-log-table
                          (where [[= :process_type interval] [= :symbol symbol] ])))]
    (if (empty? log-info)
      false
      (:completed log-info))))



(defn log-process
  [process symbol completed]
  (cql/insert (conn/dbcon)
              almanac-log-table
              {:process process
               :symbol symbol
               :completed completed}))
