(ns com.cj.data
  (:require [com.cj.orswot :as orswot]
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

(defn new [actor]
  (Data. (orswot/new actor) (orswot/new actor)))

(defn get-list [data id]
  (orswot/get id (:lists (query-dt data))))

(defn get-unique-item [data id]
  (orswot/get id (:unique-items (query-dt data))))


(defn update-list [data id new-list]
  (update :lists data orswot/update id new-list))