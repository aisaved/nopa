(ns accrue.math.stats
  (:require [incanter.stats :as stats]))

(defn standard-deviation
  "Computes standard deviation"
  [list-data]
  (stats/sd list-data))
