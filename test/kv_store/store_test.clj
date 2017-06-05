(ns kv-store.store-test
  (:use [kv-store.store]
        [clojure.test]))

(def store-atom (atom {}))
(def store (->SimpleStore store-atom))

(defn init-test []
  (reset! store-atom {}))

(deftest predicate-tests
  (testing "SetEntry Predicates"
    (testing "Complex predicate happy case"
      (init-test)
      (def predicates [(pred-equals? "1" "A") (pred-exists? "2")]) ;; only update if "1" has value "A" and key "2" exists
      (.setEntry store "1" "A") ;; set 1 => A
      (.setEntry store "2" "B") ;; set 2 => B
      (is (nil? (:errors (.setEntry store "3" "C" predicates)))) ;; set 3 => C if the predicates hold up
      (is (= (:value (.getEntry store "1")) "A"))
      (is (= (:value (.getEntry store "2")) "B"))
      (is (= (:value (.getEntry store "3")) "C")))

    (testing "equals happy case"
      (init-test)
      (.setEntry store "1" "A")
      (is (nil? (:errors (.setEntry store "2" "B" [(pred-equals? "1" "A")]))))
      (is (= (:value (.getEntry store "1")) "A"))
      (is (= (:value (.getEntry store "2")) "B")))

    (testing "equals failure case"
      (init-test)
      (is (not (nil? (:errors (.setEntry store "2" "B" [(pred-equals? "1" "A")])))))
      (is (nil? (:value (.getEntry store "2")))))

    (testing "exists happy case"
      (init-test)
      (.setEntry store "1" "A")
      (is (nil? (:errors (.setEntry store "2" "B" [(pred-exists? "1")]))))
      (is (= (:value (.getEntry store "2")) "B")))

    (testing "exists failure case"
      (init-test)
      (is (not (nil? (:errors (.setEntry store "2" "B" [(pred-exists? "2")])))))
      (is (nil? (:value (.getEntry store "2")))))))

(deftest predicate-read
  (testing "Version Didn't Change"
    (init-test)
    ;; lets assume this was placed into the store atomically, even though we can only write a single value at a time currently.
    (.setEntry store "my-key" "a")
    (.setEntry store "my-key-version" 1)
    (is (= (:value (.getEntry store "my-key" [(pred-equals? "my-key-version" 1)])) "a"))
    ;; cool, we got the value back and we know that the version didn't change on us := thus we're consistent up to this point

    ;; Assuming two clients, b and c contending over my-key.
    (.setEntry store "my-key" "b")
    (.setEntry store "my-key-version" 2)

    ;; Now client C wants update the key with key + 1 iff the key-version hasn't changed. This should fail because client b has updated my-key.
    (is (nil? (:value (.getEntry store "my-key-version" [(pred-equals? "my-key-version" 1)]))))))
