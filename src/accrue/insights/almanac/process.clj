(ns accrue.insights.almanac.process
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
            [accrue.insights.almanac.imp :as daily-almanac]
            [accrue.data.barchart :as barchart]
            [accrue.data.symbols :as symbols]
            [accrue.insights.almanac.log :as log]
            [taoensso.timbre :as timbre]))



(defn process-daily-data
  [symbol]
  (if (log/daily-data-processed? symbol)
    (timbre/info (str "Daily pattern already computed for " symbol))
    (do
      (daily-almanac/process-daily-symbol symbol)
      (log/daily-data-processed symbol))))


(defn save-daily-data
  [symbol]
  (if (log/daily-data-available? symbol)
    (do
      (timbre/info (str "Daily data available for " symbol))
      (process-daily-data symbol))
    (do
      (barchart/fetch-daily-data symbol)
      (log/daily-data-fetched symbol)
      (process-daily-data symbol))))


(defn fetch-process-daily-data
  []
  (do
    (let [all-symbols (symbols/raw-symbols)]
      (doseq [symbol all-symbols]
        (go (save-daily-data symbol))))))
