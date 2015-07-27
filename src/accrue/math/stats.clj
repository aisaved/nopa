(ns accrue.math.stats
  (:require [incanter.stats :as stats]))

(defn standard-deviation
  "Computes standard deviation"
  [list-data]
  (stats/sd list-data))



(defn percent
  "Calculates gain lose percentage"
  [part total]
  (float
    (* (/ part total) 100)))


(defn win-percent
  "calculates win percent from a list of gain-loss "
  [list-data]
  (let [win-count (reduce + (map #(if (> % 0) 1 0) list-data))]
    (percent win-count (count list-data))))


(defn average
  [list-data]
  (with-precision 2
   (/ (reduce + list-data) (count list-data))))
