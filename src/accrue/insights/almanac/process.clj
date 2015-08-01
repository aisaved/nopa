(ns accrue.insights.almanac.process
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
            [accrue.insights.almanac.daily :as daily-almanac]
            [accrue.data.barchart :as barchart]
            [accrue.data.symbols :as symbols]
            ))


(def daily-data-channel (chan 1000))
(def daily-process-channel (chan 1000))



(defn test-process [symbol]
  (Thread/sleep 3000)
  (println (str "---" symbol)))


(defn test-put []
  (doseq [each (range 30)]
    (>!! daily-data-channel each)))



(defn process-daily-data
  [symbol]
  (println "received daily data process")
  (daily-almanac/process-daily-symbol symbol))


(defn save-daily-data
  [symbol]
  (do 
    (barchart/fetch-daily-data symbol)
    (>!! daily-process-channel symbol)))



(defn init-daily-data-channel
  []
  (go 
    (while true
      (save-daily-data (<! daily-data-channel)))))


(defn init-daily-process-channel
  []
  (go 
    (while true
      (process-daily-data (<! daily-process-channel)))))


(defn init-almanac-channels []
  (do
    (init-daily-data-channel)
    (init-daily-process-channel)))


(defn fetch-process-daily-data
  []
  (do 
    (init-almanac-channels)
    (let [all-symbols (symbols/raw-symbols)]
      (doseq [symbol all-symbols]
        (>!! daily-data-channel symbol)))))
