(ns accrue.data.iqfeed
  (require
    [manifold.deferred :as d]
    [manifold.stream :as s]
    [clojure.edn :as edn]
    [aleph.tcp :as tcp]
    [gloss.core :as gloss]
    [gloss.io :as io]))


(def end-signal "!ENDMSG!,")
(def error-symbol "E,Invalid symbol.,")


(defn date-to-iqfeed-date [date-time interval]
  (let [year (t/year date-time)
        month (append-zero (t/date-to-month date-time))
        day (append-zero (t/date-to-day date-time))
        hour (append-zero (t/date-to-hour date-time))
        minute (append-zero (t/date-to-minute date-time))
        second (append-zero (t/date-to-second date-time))]
    (if (= interval "86400") ;;Daily data check for date format
      (str year month day)
      (str year month day " " hour minute second))))


(defn iqfeed-request
  [symbol interval iqfeed-start-date iqfeed-end-date]
  (if (= (str interval) "86400") ;;Daily data check for request data format
    (str "HDT," symbol "," iqfeed-start-date "," iqfeed-end-date ",,1")
    (str "HIT," symbol "," (str interval) "," iqfeed-start-date "," iqfeed-end-date ",,,,1")))

(def history-socket-params 
  {:host "127.0.0.1",
   :port 9100,
   :frame (string :utf-8 :delimiters ["\r\n"])
   })

(defn client
  [host port]
  (tcp/client {:host host, :port port})
  )


