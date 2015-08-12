(ns accrue.data.api
  (:use compojure.core)
  (:require [liberator.core :refer [resource defresource]]
            [centipair.core.contrib.response :as response]
            [accrue.data.symbols :as symbols]
            ))


(defresource accrue-symbol-api []
  :available-media-types ["application/json"]
  :handle-ok (fn [context] 
               (response/liberator-json-response (symbols/get-symbol (get-in context [:request :params])))))



(defroutes api-data-routes
  (GET "/api/data/symbol" [] (accrue-symbol-api)))

