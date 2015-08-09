(ns accrue.insights.almanac.calc
  (:require [accrue.utilities.time :as t]
            [accrue.math.stats :as stats]))



(def pattern-current-year (t/current-year))

(defn gl-percent
  "Calculates gain lose percentage"
  [open close]
  (with-precision 5
    (* (/ (- close open) open) 100)))


(defn ohlc-group-gl-percent
  [ohlc-group]
  (let [open (:open (first ohlc-group))
        close (:close (first ohlc-group))]
    (gl-percent open close)))


(defn years-passed
  [key-data]
  (- pattern-current-year (t/timestamp-to-year (:time key-data))))


(defn find-yearly-sd
  [percent-gain-years]
  (map #(stats/standard-deviation (take % percent-gain-years))
       (range 5 (count percent-gain-years))))

(defn find-yearly-win-percent
  [percent-gain-years]
  (map #(stats/win-percent (take % percent-gain-years))
       (range 5 (count percent-gain-years))))


(defn find-yearly-average-gl-percent
  [percent-gain-years]
  (map #(stats/average (take % percent-gain-years))
       (range 5 (count percent-gain-years))))

