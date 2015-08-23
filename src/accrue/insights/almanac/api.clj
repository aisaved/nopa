(ns accrue.insights.almanac.api
  (:use compojure.core)
   (:require [liberator.core :refer [resource defresource]]
             [centipair.core.contrib.response :as response]
             [accrue.insights.almanac.query :as query]))



(defresource almanac-pattern-api []
  :available-media-types ["application/json"]
  :malformed? (fn [context]
                (let [params (get-in context [:request :params])]
                  (if (nil? (:type params))
                    true
                    (nil? (some #{(:type params)} ["monthly" "daily" "weekly"])))))
  :handle-ok (fn [context] 
               (response/liberator-json-response (query/pattern-query (get-in context [:request :params])))))



(defroutes api-almanac-pattern-routes
  (GET "/api/almanac/pattern" [] (almanac-pattern-api)))
