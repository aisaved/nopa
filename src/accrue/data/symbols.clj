(ns accrue.data.symbols
  (:use 
   ;;clojure.java.io
   centipair.core.db.connection)
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [accrue.utilities.file :as file]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [korma.core :as korma :refer [insert
                                          delete
                                          select
                                          where
                                          set-fields
                                          values
                                          fields
                                          offset
                                          limit
                                          defentity]]))


(defentity symbols)

(defn save-company-list
  [each]
  (if (= "Symbol" (first each))
    (println "company list header")
    
    (insert symbols (values {:symbol (nth each 0)
                             :name (nth each 1)
                             :market_cap (read-string (nth each 3))
                             :sector (nth each 6)
                             :industry (nth each 7)}))))


(defn save-nya-index
  [each]
  (if (= "NAME" (first each))
    (println "nya index header")
    
    (insert symbols (values {:symbol (nth each 1)
                             :name (nth each 0)
                             ;;:market_cap (read-string (nth each 3))
                             :sector (nth each 6)
                             :industry (nth each 4)}))))

(defn save-symbol-info
  [each]
  (if (= "Symbol" (first each))
    (println "symbol info header")
    
    (insert symbols (values {:symbol (first each)
                             :name (last each)
                             ;;:market_cap (read-string (nth each 3))
                             ;;:sector (nth each 6)
                             ;;:industry (nth each 4)
                             }))))


(defn company-list
  []
    (with-open [rdr (io/reader "files/company-list.csv")]
      (doall
       (map save-company-list (csv/read-csv rdr)))))

(defn nya-index
  []
  (with-open [rdr (io/reader "files/nya-index.csv")]
      (doall
        (map save-nya-index (csv/read-csv rdr)))))

(defn symbol-info
  []
  (with-open [rdr (io/reader "files/symbol-info.csv")]
      (doall
       (map save-symbol-info (csv/read-csv rdr)))))


(defn load-symbols
  []
  (do 
    (company-list)
    (nya-index)
    (symbol-info)))


(defn search-symbol
  [query]
  (let [q (:q query)]
    (select symbols (where {:symbol [like (str q "%")]}))))


(defn select-all-symbols
  []
  (select symbols))


(defn raw-symbols
  []
  (map #(:symbol %) (select symbols)))




(def iqfeed-symbol-uri "http://www.dtniq.com/product/mktsymbols_v2.zip")
(def symbols-table "symbols")

(defn save-iqfeed-symbol
  [lines]
  (doseq [line lines]
    (let [symbol-data (clojure.string/split line #"\t")]
      (cql/insert (dbcon)
                  symbols-table
                  {:symbol (first symbol-data)
                   :description (second symbol-data)
                   :exchange (nth symbol-data 2)
                   :listed_market (nth symbol-data 3)
                   :security_type (nth symbol-data 4)}))))

(defn unzip-iqfeed-file []
  (file/unzip-read-text-file "files/mktsymbols_v2.zip" save-iqfeed-symbol))

(defn download-iqfeed-symbol-file
  []
  (file/download-file iqfeed-symbol-uri "files/mktsymbols_v2.zip"))


(defn get-symbol 
  [params]
  (let [symbol-obj (first (cql/select (dbcon) symbols-table (query/where [[= :symbol (:symbol params)]])))]
    {:status (if (nil? symbol-obj) 404 200)
     :result symbol-obj}))
