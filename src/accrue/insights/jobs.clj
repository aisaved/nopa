(ns accrue.insights.jobs
  (:require [accrue.insights.almanac.process :as almanac-process]
            [immutant.scheduling :refer :all]
            [accrue.insights.almanac.test :as test-process]
            ))


(defn start-almanac-process
  []
  (schedule almanac-process/fetch-process-daily-data (in 5 :seconds)))


(defn start-test-process []
  (schedule test-process/start-build (in 5 :seconds)))
