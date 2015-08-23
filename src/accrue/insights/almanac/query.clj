(ns accrue.insights.almanac.query
  (:require [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]
            [clojurewerkz.cassaforte.cql :as cql]
            [accrue.insights.almanac.models :as almanac-model]
            [validateur.validation :refer :all]))


(defn fetch-by-day-pattern
  [day-key pattern-length]
  (let [date-id (str day-key "-" pattern-length)]
    (cql/select (conn/dbcon)
                almanac-model/almanac-daily-table
                (where [[= :date_id date-id]]))))


(defn generate-daily-pattern-key-param
  [params]
  [= :date_id (str "d-" (:day params) "-" (:month params) "-"  (:pattern-length params))])


(defn generate-monthly-pattern-key-param
  [params]
  [= :date_id (str "m-" (:month params))])


(defn generate-month-weekly-pattern-key-param
  [params]
  [= :date_id (str "mw-" (:month params) "-" (:week params))])

(defn generate-weekly-pattern-key-param
  [params]
  [= :date_id (str "w-" (:week params))])


(defn generate-quarter-weekly-pattern-key-param
  [params]
  [= :date_id (str "w-" (:week params))])


(defn generate-symbol-param
  [params]
  (if (nil? (:symbol params))
    nil
    [= :symbol (:symbol params)]))


(defn years-filter
  [filter-function data-row params]
  (let [filtered-numerals (map (partial filter-function data-row params)
                               (range (:history-range-start params) (+ 1 (:history-range-end params))))
        filtered-result (reduce + filtered-numerals)]
    (> filtered-result 0)))



(defn sd-year-filter
  [data-row params year]
  (let [sd-field ((keyword (str "sd_" year)) data-row)]
    (if (nil? sd-field)
      0
      (if (< sd-field (:sd params))
        1
        0))))


(defn sd-filter
  [params data-group]
  (if (first data-group)
    (if (nil? (:sd params))
      [true (second data-group)]
      [(years-filter sd-year-filter (second data-group) params)
       (second data-group)])
    data-group))



(defn position
  [data-row params year]
  (let [accuracy-field ((keyword (str "accuracy_range_" year)) data-row)]
    (if (nil? accuracy-field)
      false
      (if (= (:position params) "short")
        (< accuracy-field (- 100 (:accuracy-range params)))
        (> accuracy-field (:accuracy-range params))))))

(defn accuracy-year-filter
  [data-row params year]
  (let [consider (position data-row params year)]
    (if consider 1 0)))


(defn accuracy-filter
  [params data-group]
  (if (first data-group)
    (if (nil? (:accuracy-range params))
      [true (second data-group)]
      [(years-filter accuracy-year-filter (second data-group)  params)
       (second data-group)])
    data-group))



(defn gl-year-filter
  [data-row params year]
  (let [gl-field ((keyword (str "gl_percent_" year)) data-row)]
    (if (nil? gl-field)
      0
      (if (and (> gl-field (:gl-range-start params)) (< gl-field (:gl-range-end params)))
        1
        0))))

(defn gl-filter
  [params data-group]
  (if (first data-group)
    (if (or (nil? (:gl-range-start params)) (nil? (:gl-range-end params)))
      [true (second data-group)]
      [(years-filter gl-year-filter (second data-group)  params)
       (second data-group)])
    data-group))


(defn query-filter
  [params]
  (comp
   first
   (partial gl-filter params)
   (partial sd-filter params)
   (partial accuracy-filter params)
   (fn [each]
     [true each])))


(defn clean-week
  [params]
  (assoc params :week (Integer. (:week params))))


(defn clean-quarter
  [params]
  (assoc params :quarter (Integer. (:quarter params))))


(defn clean-day
  [params]
  (assoc params :day (Integer. (:day params))))


(defn clean-month
  [params]
  (assoc params :month (Integer. (:month params))))


(defn clean-history-range
  [params]
  (assoc params
         :history-range-start (Integer. (:history-range-start params))
         :history-range-end (Integer. (:history-range-end params))))


(defn clean-gl-range
  [params]
  (assoc params
         :gl-range-start (Integer. (:gl-range-start params))
         :gl-range-end (Integer. (:gl-range-end params))))

(defn clean-pattern-length
  [params]
  (assoc params :pattern-length (Integer. (:pattern-length params))))


(defn clean-sd
  [params]
  (assoc params :sd (Integer. (:sd params))))

(defn clean-accuracy-range
  [params]
  (assoc params :accuracy-range (Integer. (:accuracy-range params))))


(def clean-params
  (comp
   clean-accuracy-range
   clean-sd
   clean-pattern-length
   clean-gl-range
   clean-history-range))


(def clean-daily-params
  (comp
   clean-month
   clean-day))


(defn db-query
  [pattern-key params]
  (let [db-query (filter #(not (nil? %))
                         [pattern-key
                          (generate-symbol-param params)])
        db-results (cql/select (conn/dbcon)
                               almanac-model/almanac-daily-table
                               (where db-query))]
    (filter (query-filter params) db-results)))


(defn daily-pattern-query
  "{:day 7 :month 8
  :gl-range-start 5 :gl-range-end 10
  :accuracy-range 70
  :position long/short
  :sd 5
  :history-range-start 5 :history-range-end 10
  :pattern-length 5
  :interval 'daily'
  :symbol '<optional>'}"
  [clean-params]
  (let [params (clean-daily-params clean-params)
        pattern-key (generate-daily-pattern-key-param params)]
    (db-query pattern-key params)))


(defn monthly-pattern-query
  "{:month 8
  :gl-range-start 5 :gl-range-end 10
  :accuracy-range 70
  :position long/short
  :sd 5
  :history-range-start 5 :history-range-end 10
  :pattern-length 5
  :interval 'monthly'
  :symbol '<optional>'}"
  [clean-params]
  (let [params (clean-month clean-params)
        pattern-key (generate-monthly-pattern-key-param params)]
    (db-query pattern-key params)))



(defn month-weekly-pattern-query
  "{:month 8
  :week 1
  :gl-range-start 5 :gl-range-end 10
  :accuracy-range 70
  :position long/short
  :sd 5
  :history-range-start 5 :history-range-end 10
  :pattern-length 5
  :interval 'monthly'
  :symbol '<optional>'}"
  [clean-params]
  (let [params (clean-month clean-params)
        pattern-key (generate-month-weekly-pattern-key-param params)]
    (db-query pattern-key params)))


(defn quarter-weekly-pattern-query
  "{:week 1
  :quarter 2
  :gl-range-start 5 :gl-range-end 10
  :accuracy-range 70
  :position long/short
  :sd 5
  :history-range-start 5 :history-range-end 10
  :pattern-length 5
  :interval 'monthly'
  :symbol '<optional>'}"
  [clean-params]
  (let [params (clean-month clean-params)
        pattern-key (generate-quarter-weekly-pattern-key-param params)]
    (db-query pattern-key params)))



(defn weekly-pattern-query
  "{:month 52
  :gl-range-start 5 :gl-range-end 10
  :accuracy-range 70
  :position long/short
  :sd 5
  :history-range-start 5 :history-range-end 10
  :pattern-length 5
  :interval 'weekly'
  :symbol '<optional>'}"
  [clean-params]
  (let [params (clean-week clean-params)]
    (if (:month params)
      (month-weekly-pattern-query params)
      (if (:quarter params)
        (quarter-weekly-pattern-query params)
        (let [params (clean-week clean-params)
              pattern-key (generate-weekly-pattern-key-param params)]
          (db-query pattern-key params))))))


(defn pattern-query
  [raw-params]
  (let [clean-params (clean-params raw-params)]
    (case (:type raw-params)
      "daily" (daily-pattern-query clean-params)
      "monthly" (monthly-pattern-query clean-params)
      "weekly" (weekly-pattern-query clean-params))))
