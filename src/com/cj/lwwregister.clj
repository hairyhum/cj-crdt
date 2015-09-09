(ns com.cj.lwwregister
  (:refer-clojure :exclude [update])
  (:require [clj-time.core :as t]
            [clj-time.coerce :as coerce])
  (:use com.cj.crdt))

(defn get-timestamp [] (coerce/to-long (t/now)))

(defrecord LwwRegister [value timestamp]
  StateCRDT
  (query-dt [register] (:value register))
  (update-dt [register value]
    (LwwRegister. value (get-timestamp)))
  (merge-dt [ours theirs]
    (if (> (:timestamp ours) (:timestamp theirs))
      ours
      theirs)))

(defn new [value] 
  (LwwRegister. value (get-timestamp)))

(defn update [register value] 
  (update-dt register value))

(defn value [register] 
  (query-dt register))

