(ns accrue.data.symbols
  (:use 
   clojure.java.io
   korma.core
   accrue.db.connect)
  (:require [clojure.data.csv :as csv]))


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
    (with-open [rdr (reader "files/company-list.csv")]
      (doall
       (map save-company-list (csv/read-csv rdr)))))

(defn nya-index
  []
  (with-open [rdr (reader "files/nya-index.csv")]
      (doall
        (map save-nya-index (csv/read-csv rdr)))))

(defn symbol-info
  []
  (with-open [rdr (reader "files/symbol-info.csv")]
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
