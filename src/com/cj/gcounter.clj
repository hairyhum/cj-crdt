(ns com.cj.gcounter
  (:refer-clojure :exclude [inc dec])
  (:require [com.cj.vv :as vv])
  (:use [com.cj.crdt]))

(defrecord GCounter [actor payload]
  StateCRDT
  (query-dt [counter]
    (vv/summ-all (:payload counter)))
  (update-dt [counter i]
    (let [actor (:actor counter)]
      (update counter :payload vv/increment actor i)))
  (merge-dt [our their]
    (update our :payload vv/merge (:payload their))))

(defn new
  ([actor i] 
    (GCounter. actor
              (vv/new {actor i})))
  ([] (com.cj.gcounter/new :default-actor 0)))

(defn inc [counter value]
  (update counter value))

(defn dec [counter value]
  (update counter (- value)))
