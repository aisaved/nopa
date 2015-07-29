(ns accrue.insights.almanac.models
  (:require 
            [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]
            [accrue.utilities.time :as t]
            ))



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
  (let [date-id (partition-key day-key (:pattern-length n-day-pattern))]
    (into {} 
          [{:date-id date-id
            :symbol (:symbol n-day-pattern)}
           (generate-n-years-map "gl_percent" (:avg-gl-percent n-day-pattern))
           (generate-n-years-map "sd" (:sd n-day-pattern))
           (generate-n-years-map "accuracy_range" (:win-percent n-day-pattern))
           ])))
