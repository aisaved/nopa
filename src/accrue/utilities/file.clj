(ns accrue.utilities.file
  (:require [clojure.java.io :as io])
  (:import (java.util.zip ZipInputStream)))


(defn download-file
  [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))


(defn unzip-read-text-file
  "Unzips and reads a text file line by line
  Returns lines array"
  [file handler]
  (let [zip-file (ZipInputStream. (io/input-stream file))
        zip-entry (.getNextEntry zip-file)
        ;;file-name  (.getName zip-entry)
        ;;file-size (.getSize zip-entry)
        ;;bytes (byte-array file-size)
        ]
    ;;(println file-name)
    ;;(println file-size)
    ;;(.read zip-file bytes 0 file-size) 
    ;;(String. bytes  "UTF-8")
    (with-open [rdr (io/reader zip-file)]
      (let [lines (line-seq rdr)]
        (handler lines)))))
