(ns accrue.insights.almanac.query
  (:require [clojurewerkz.cassaforte.query :refer :all]
            [centipair.core.db.connection :as conn]
            [clojurewerkz.cassaforte.cql :as cql]
            [accrue.insights.almanac.models :as almanac-model]))


(defn fetch-by-day-pattern
  [day-key pattern-length]
  (let [date-id (str day-key "-" pattern-length)]
    (cql/select (conn/dbcon)
                almanac-model/almanac-daily-table
                (where [[= :date_id date-id]]))))


(defn generate-daily-pattern-key-param
  [params]
  [= :date_id (str (:day params) "-" (:month params) "-"  (:pattern-length params))])


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




(defn clean-day [params]
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
   clean-history-range
   clean-month
   clean-day))


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
  [raw-params]
  (let [params (clean-params raw-params)
        db-query (filter #(not (nil? %))
                         [(generate-daily-pattern-key-param params)
                          (generate-symbol-param params)])
        db-results (cql/select (conn/dbcon)
                               almanac-model/almanac-daily-table
                               (where db-query))]
    (filter (query-filter params) db-results)))
