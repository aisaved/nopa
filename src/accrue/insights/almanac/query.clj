(ns accrue.insights.almanac.query
  (:require [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]
            [clojurewerkz.cassaforte.cql :as cql]
            [accrue.insights.almanac.models :as almanac-model]))


(defn fetch-by-day-pattern
  [day-key pattern-length]
  (let [date-id (str day-key "-" pattern-length)]
    (cql/select (conn/dbcon)
                almanac-model/almanac-daily-table
                (where [[= :date_id date-id]]))))
