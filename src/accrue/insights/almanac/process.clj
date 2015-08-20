(ns accrue.insights.almanac.process
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
            [accrue.insights.almanac.daily :as daily-almanac]
            [accrue.insights.almanac.monthly :as monthly-almanac]
            [accrue.insights.almanac.weekly :as weekly-almanac]
            [accrue.data.barchart :as barchart]
            [accrue.data.symbols :as symbols]
            [accrue.insights.almanac.log :as log]
            [taoensso.timbre :as timbre]
            [accrue.constants :as c]
            ))



(defn get-available-threads []
  (let [available-cores (.availableProcessors (Runtime/getRuntime))]
    (if (> available-cores 10)
      10
      available-cores)))

(def threads (get-available-threads))


(defn process-weekly-data
  [symbol]
  (if (log/data-processed? c/almanac-weekly symbol)
    (timbre/info (str "Weekly pattern already computed for " symbol))
    (do
      (weekly-almanac/process-weekly-pattern symbol)
      (log/log-process c/almanac-weekly symbol true))))

(defn process-month-weekly-data
  [symbol]
  (if (log/data-processed? c/almanac-month-weekly symbol)
    (timbre/info (str "Month Weekly pattern already computed for " symbol))
    (do
      (weekly-almanac/process-month-weekly-pattern symbol)
      (log/log-process c/almanac-month-weekly symbol true))))


(defn process-monthly-data
  [symbol]
  (if (log/data-processed? c/almanac-monthly symbol)
    (timbre/info (str "Monthly pattern already computed for " symbol))
    (do
      (monthly-almanac/process-monthly-pattern symbol)
      (log/log-process c/almanac-monthly symbol true))))


(defn process-daily-data
  [symbol]
  (if (log/data-processed? c/almanac-daily symbol)
    (timbre/info (str "Daily pattern already computed for " symbol))
    (do
      (daily-almanac/process-daily-symbol symbol)
      (log/log-process c/almanac-daily symbol true))))


(defn process-data [symbol]
  (do 
    (process-monthly-data symbol)
    (process-weekly-data symbol)
    (process-month-weekly-data symbol)
    (process-daily-data symbol)))


(defn save-daily-data
  [symbol]
  (if (log/data-available? c/data-daily symbol)
    (do
      (timbre/info (str "Daily data available for " symbol))
      (process-data symbol))
    (do
      (barchart/fetch-daily-data symbol)
      (log/log-process c/data-daily symbol true)
      (process-data symbol))))


(defn process-symbol-group
  [symbol-group]
  (doseq [symbol symbol-group]
    (save-daily-data symbol)))


(defn fetch-process-daily-data
  []
  (do
    (let [all-symbols (symbols/raw-symbols)
          symbols-count (count all-symbols)
          n-partition (quot symbols-count threads)
          symbol-groups (partition-all n-partition all-symbols)]
      (doseq [symbol-group symbol-groups]
        (go
          (process-symbol-group symbol-group))))))
