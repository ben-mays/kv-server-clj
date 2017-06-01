(ns kv-store.store)

(defprotocol IStore
  "A abstract interface for interacting with the underlying KV store"
  (setEntry [this key val] [this key val predicates] "Put val into the store, under the key given. Returns a map with :value set to the new value of the key, or :errors seto a error message, with :results as a list of the predicate evaluations.")
  (getEntry [this key] [this key predicates] "Retrieves the value associated with key, returning a map with :value set to the value or :errors set to a error message."))

;; We want to define a few predicates:
;;  * equals key val
;;  * exists key
;;  * not-exists key

;; Each predicate constructor returns an anonymous function that contains the args passed
(defn pred-equals? [key val]
  (fn [store] (= (:value (.getEntry store key)) val)))

(defn pred-exists? [key]
  (fn [store] (not (nil? (:value (.getEntry store key))))))

(defn pred-absent? [key]
  (fn [store] (nil? (:value (.getEntry store key)))))

;; A simple KV store implementation based on a Clojure atom containing a map.
;; Assumption that kv passed here is an atom; e.g. (def kv (atom {}))
(deftype SimpleStore [^{:volatile-mutable true} kv]
  IStore

  (setEntry
    [this key val]
    (swap! kv assoc key val)
    {:value val :errors nil})

  (setEntry
    [this key val predicates]
    (locking kv
      (let [results (map #(% this) predicates)]
        (if (every? true? results)
          ;; all of the predicates are true, perform the write
          (.setEntry this key val)
          {:value nil :errors "Predicate failed." :results results}))))

  (getEntry
    [this key]
    {:value (get @kv key) :errors nil})

  (getEntry
    [this key predicates]
    (locking @kv
      (let [results (map (fn [pred] (pred this)) predicates)]
        (if (every? true? results)
          ;; all of the predicates are true, perform the read
          (.getEntry this key)
          {:value nil :errors "Predicates failed." :results results})))))
