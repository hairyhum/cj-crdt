(ns com.cj.data
  (:require [com.cj.storage :as storage]
           [com.cj.crdt :as crdt])
  (:use [com.cj.crdt]))

(defrecord Data [lists unique-items]
  StateCRDT
  (query-dt [data] data)
  (update-dt [data [scope action & args]]
    (action (scope data) args))
  (merge-dt [ours theirs]
    (let [merged-lists (merge-dt (:lists ours) (:lists theirs))
          merged-unique-items (merge-dt (:unique-items ours) (:unique-items theirs))]
      (Data. merged-lists merged-unique-items))))

(defn new
  [actor] (Data. (storage/new actor) (storage/new actor)))

