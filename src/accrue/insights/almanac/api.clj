(ns accrue.insights.almanac.api
  (:use compojure.core)
   (:require [liberator.core :refer [resource defresource]]
             [centipair.core.contrib.response :as response]
             [centipair.core.utilities.validators :as v]
             [accrue.insights.almanac.query :as query]))


(defn valid-daily-params?
  [params]
  (and (v/valid-number? (:day params)) (v/valid-number? (:month params))))

(defn valid-monthly-params?
  [params]
  (v/valid-number? (:month params)))


(defn valid-weekly-params?
  [params]
  (if (v/valid-number? (:week params))
    (let [sup (or (:quarter params) (:month params))]
      (if (nil? sup)
        true
        (v/valid-number? sup)))
    false))


(defn malformed?
  [params]
  (case (:type params)
    "daily" (not (valid-daily-params? params))
    "weekly" (not (valid-weekly-params? params))
    "monthly" (not (valid-monthly-params? params))
    true))


(defresource almanac-pattern-api []
  :available-media-types ["application/json"]
  :malformed? (fn [context]
                (malformed? (get-in context [:request :params])))
  :handle-ok (fn [context] 
               (response/liberator-json-response (query/pattern-query (get-in context [:request :params])))))



(defroutes api-almanac-pattern-routes
  (GET "/api/almanac/pattern" [] (almanac-pattern-api)))
