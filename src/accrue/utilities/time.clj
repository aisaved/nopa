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

(defn timestamp-to-year
  "Returns the year from the given timestamp"
  [timestamp]
  (let [datetime (c/from-sql-date timestamp)]
    (t/year datetime)))


(defn date-to-year
  "Returns the year from the given datetime value"
  [date]
  (t/year date))


(defn current-year []
  "returns current year"
  (t/year (t/now)))
