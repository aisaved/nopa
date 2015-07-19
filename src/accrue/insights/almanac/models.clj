(ns accrue.insights.almanac.models
  (:require [clojurewerkz.cassaforte.cql    :as cql]
            [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]))


(defn date-id
  "converts date to date-id format
  used as primary key in almanac table
  date-id format:<day>-<month>-<past years>
  Example : 1-07-25"
  [date past-years]
  
  )


(defn process-daily-symbol [symbol]
  
  )
