(ns centipair.init
  (:require [centipair.core.components.notifier :as notifier]
            [centipair.core.csrf :as csrf]))



(defn ^:export init! []
  (do
    ;;(dashboard-menu/render-admin-menu)
    (notifier/render-notifier-component)
    (csrf/fetch-csrf-token)))
