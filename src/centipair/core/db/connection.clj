(ns centipair.core.db.connection
  (:use korma.db)
  (:require [centipair.core.config :as config]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql    :as cql]))



(defdb db (postgres config/sql-db-config))


(defn get-db-connection
  "Gets db connection for cassandra"
  []
  (let [conn (cc/connect (:cluster config/cassandra-config) )]
    (cql/use-keyspace conn (:keyspace config/cassandra-config))
    conn))

(def conn (atom nil))

(defn dbcon
  "Use this function as connection in cassandra related functions"
  []
  (if (nil? @conn) 
    (reset! conn (get-db-connection))
    @conn))


(defn warm? []
  (let [warm-connection (dbcon)]
    (if (nil? warm-connection)
      false
      true)))
