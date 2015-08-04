(ns centipair.core.init
  (:require [centipair.core.channels :as core-channels]
            [centipair.core.db.connection :as conn]
            [taoensso.timbre :as timbre]
            ))


(defn init-system []
  (do
    (if (conn/warm?)
      (timbre/info "Db connection success")
      (timbre/info "Db connection error"))
    (core-channels/init-core-channels)))
