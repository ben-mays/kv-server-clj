(ns kv-store.handler
  (:use [kv-store.store])
  (:require [compojure.core :refer :all]
            [compojure.route :as compjure.route]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :as ring.response]))

(declare store)

(def store (->SimpleStore (atom {})))

(defn parse-constraint
  [constraint]
  (let [{:keys [type key val]} constraint]
    (condp = type
      "exists" (pred-exists? key)
      "equals" (pred-equals? key val)
      "absent" (pred-absent? key)
      (constantly true))))

(defn parse-constraints
  [body]
  (map parse-constraint (:constraints body)))

(defn get-key-handler
  [request]
  (let [key   (:key (:body request))
        preds   (parse-constraints (:body request))
        res   (.getEntry store key preds)]
    (.println System/out key )
    (ring.response/response {:key     key
                             :response res})))

(defn set-key-handler
  [request]
  (let [key (:key (:body request))
        val (:val (:body request))
        res (.setEntry store key val)]
    (ring.response/response {:key      key
                             :response res})))

(defroutes app-routes
  (POST "/get" [] get-key-handler)
  (POST "/set" [] set-key-handler)
  (compojure.route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})))
