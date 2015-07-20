(ns accrue.almanac.calendar
  (:require [accrue.utilities.time :as t]))


(def month-days-number
  {:1 31 :2 29 :3 31 :4 30
   :5 31 :6 30 :7 31 :8 31
   :9 30 :10 31 :11 30 :12 31})


(defn generate-month-days
  [month]
  (let [days (range 1 (+ 1 ((keyword month) month-days-number)))]
    
    ))

(defn generate-all-days
  []
  (let [months (range 1 13)]
    
    ))
