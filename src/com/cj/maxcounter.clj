(ns com.cj.maxcounter
  (:refer-clojure :exclude [set])
  (:use [com.cj.crdt]))

(defrecord Maxcounter [value]
  StateCRDT 
  (query-dt [counter] (:value counter))
  (update-dt [counter new-value]
    (if (> new-value (:value counter))
      (assoc counter :value new-value)
      counter))
  (merge-dt [our their]
    (assoc our :value (max (:value our) (:value their)))))

(defn new
  ([] 0)
  ([value] value))

(defn set [counter val] (update-dt counter val))