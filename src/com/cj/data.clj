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

(defn add-list [data list]
  (update :lists data orswot/add list))

(defn get-list [data id]
  (orswot/get (:lists (query-dt data)) id))

(defn update-list [data id new-list]
  (update :lists data orswot/update id new-list))

(defn remove-list [data id]
  (update :lists data orswot/remove (get-list data id)))  

(defn update-list-with [data id fun & args]
  (update :lists data orswot/update id (apply fun (get-list id) args)))

(defn add-unique-item [data unique-item]
  (update :unique-items data orswot/add unique-item))

(defn get-unique-item [data id]
  (orswot/get (:unique-items (query-dt data)) id))

(defn update-unique-item [data id unique-item]
  (update :unique-items data orswot/update id unique-item))

(defn remove-unique-item [data id]
  (update :unique-items data orswot/remove (get-unique-item data id)))  


