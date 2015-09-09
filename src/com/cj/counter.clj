(ns com.cj.counter
  (require [com.cj.vv :as vv]))

(use '[com.cj.crdt :only (StateCRDT)])

(defrecord Counter [actor payload]
  StateCRDT
  (query-dt [counter]
    (vv/summ-all (:payload counter)))
  (update-dt [counter i]
    (let [actor (:actor counter)]
      (update counter :payload vv/increment actor i)))
  (merge-dt [our their]
    (update our :payload vv/merge (:payload their))))

(defn new-counter
  ([] (new-counter ::default-actor 0))
  ([actor i] 
    (Counter. actor
              (vv/new {actor i}))))

(defn increment-counter [counter value]
  (update counter value))

(defn decrement-counter [counter value]
  (update counter (- value)))
