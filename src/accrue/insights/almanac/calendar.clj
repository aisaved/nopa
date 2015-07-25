(ns accrue.insights.almanac.calendar
  (:require [accrue.utilities.time :as t]))


(def month-days-number
  {:1 31 :2 29 :3 31 :4 30
   :5 31 :6 30 :7 31 :8 31
   :9 30 :10 31 :11 30 :12 31})


(defn generate-month-days
  [month]
  (let [days (range 1 (+ 1 ((keyword (str month)) month-days-number)))]
    (for [day days]
      (keyword (str day "-" month)))))

(defn generate-all-days
  []
  (let [months (range 1 13)]
    (reduce (fn [previous next] (concat previous next)) (map generate-month-days months))))


(defonce all-days (generate-all-days))


(defn find-next-day-key
  [day-key]
  (let [day-key-index (.indexOf all-days day-key)]
    (if (= day-key-index 365)
      (first all-days)
      (nth all-days (+ day-key-index 1)))))
