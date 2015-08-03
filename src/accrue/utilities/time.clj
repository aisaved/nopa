(ns accrue.utilities.time
  (:require 
   [clj-time.local :as l]
   [clj-time.core :as t]
   [clj-time.coerce :as c]
   [clj-time.format :as f]))



(defn to-sql-time
  "Converts to sql time for database"
  [timestamp]
  (c/to-sql-time timestamp))

(defn to-long
  [timestamp]
  (c/to-long timestamp))

(defn date-to-year
  "Returns the year from the given datetime value"
  [date]
  (t/year date))


(defn date-to-month
  "Returns the month from the given datetime value"
  [date]
  (t/month date))


(defn date-to-day
  "Returns the day from the given datetime value"
  [date]
  (t/day date))

(defn date-to-hour
  [date]
  (t/hour date))

(defn date-to-minute
  [date]
  (t/minute date))


(defn date-to-second
  [date]
  (t/second date))




(defn timestamp-to-year
  "Returns the year from the given timestamp"
  [timestamp]
  (let [datetime (c/from-sql-date timestamp)]
    (date-to-year datetime)))

(defn timestamp-to-month
  "Returns the year from the given timestamp"
  [timestamp]
  (let [datetime (c/from-sql-date timestamp)]
    (date-to-month datetime)))

(defn timestamp-to-day
  "Returns the year from the given timestamp"
  [timestamp]
  (let [datetime (c/from-sql-date timestamp)]
    (date-to-day datetime)))


(defn current-year []
  "returns current year"
  (date-to-year (t/now)))


(defn now []
  (t/now))



(defn history-range
  [past-years]
  (let [history-year (- (current-year) past-years)]
    ({:start-date (t/date-time  history-year 1 2 9 3 27 456)
      :end-date (now)})))
