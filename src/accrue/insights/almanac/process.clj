(ns accrue.insights.almanac.process
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
            [accrue.insights.almanac.daily :as daily-almanac]
            [accrue.insights.almanac.monthly :as monthly-almanac]
            [accrue.data.barchart :as barchart]
            [accrue.data.symbols :as symbols]
            [accrue.insights.almanac.log :as log]
            [taoensso.timbre :as timbre]
            [accrue.constants :as c]
            ))



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


(defn save-daily-data
  [symbol]
  (if (log/data-available? c/data-daily symbol)
    (do
      (timbre/info (str "Daily data available for " symbol))
      (process-monthly-data symbol)
      (process-daily-data symbol))
    (do
      (barchart/fetch-daily-data symbol)
      (log/log-process c/data-daily symbol true)
      (process-monthly-data symbol)
      (process-daily-data symbol))))


(defn fetch-process-daily-data
  []
  (do
    (let [all-symbols (symbols/raw-symbols)]
      (doseq [symbol all-symbols]
        (go (save-daily-data symbol))))))
