(ns kv-store.handler
  (:use [kv-store.store])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response]]))

(declare store)

(def store (->SimpleStore (atom {})))

(defn get-key-handler [request]
  (let [key (:key (:params request))]
    (response {:key key :response (.getEntry store key)})))

(defn set-key-handler [request]
  (let [key (:key (:params request))
        val (:val (:params request))
        res (.setEntry store key val)]
    (response {:key key :response res})))

(defroutes app-routes
  (GET "/v1/get/:key" [key] get-key-handler)
  (GET "/v1/set/:key" [key] set-key-handler)
  (route/not-found "Not Found"))

(def app
  (-> app-routes 
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults site-defaults)))
