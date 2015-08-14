(ns accrue.insights.almanac.models
  (:require [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]
            [clojurewerkz.cassaforte.cql :as cql]
            [accrue.utilities.time :as t]))

(def almanac-daily-table "almanac_daily")


(defn date-id-time
  "converts date to date-id format
  used as primary key in almanac table
  date-id format:<day>-<month>-<pattern-length>
  Example : 1-07-25"
  [date pattern-length]
  (str (t/date-to-day date) "-" (t/date-to-month date) "-" pattern-length))



(defn partition-key
  [day-key pattern-length]
  (clojure.string/replace (str day-key "-" pattern-length) #":" "" ))


(defn generate-n-years-map
  [key past-years-list]
  (into
   {}
   (keep-indexed (fn [index each]
                   [(keyword (str key "_" each)) (nth past-years-list index)]
                   ) (range 5 (+ 5 (count past-years-list))))))


(defn save-data
  [day-key n-day-pattern]
  (let [date-id (partition-key day-key (:pattern-length n-day-pattern))
        params (into {}
                     [{:date_id date-id
                       :symbol (:symbol n-day-pattern)}
                      (generate-n-years-map "gl_percent" (:avg-gl-percent n-day-pattern))
                      (generate-n-years-map "sd" (:sd n-day-pattern))
                      (generate-n-years-map "accuracy_range" (:win-percent n-day-pattern))
                      ])]
    (cql/insert (conn/dbcon) almanac-daily-table params)))


(defn save-group-data
  [day-key filled-data]
  (doseq [n-day (range 100)]
    (save-data day-key {:symbol (:symbol filled-data)
                        :pattern-length (+ 1 n-day)
                        :avg-gl-percent (nth (:avg-gl-percent filled-data) n-day)
                        :sd (nth (:sd filled-data) n-day)
                        :win-percent (nth (:win-percent filled-data) n-day)})))


(defn save-monthly-data
  [monthly-data-map]
  (let [pkey (str "m-" (:month monthly-data-map))
         params (into {}
                 [{:date_id pkey
                   :symbol (:symbol monthly-data-map)}
                  (generate-n-years-map "gl_percent" (:avg-gl-percent monthly-data-map))
                  (generate-n-years-map "sd" (:sd monthly-data-map))
                  (generate-n-years-map "accuracy_range" (:win-percent monthly-data-map))])]
    (cql/insert (conn/dbcon) almanac-daily-table params)))


(defn save-weekly-data
  [monthly-data-map]
  (let [pkey (str "w-" (:week monthly-data-map))
         params (into {}
                 [{:date_id pkey
                   :symbol (:symbol monthly-data-map)}
                  (generate-n-years-map "gl_percent" (:avg-gl-percent monthly-data-map))
                  (generate-n-years-map "sd" (:sd monthly-data-map))
                  (generate-n-years-map "accuracy_range" (:win-percent monthly-data-map))])]
    (cql/insert (conn/dbcon) almanac-daily-table params)))


