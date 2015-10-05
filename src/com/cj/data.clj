(ns com.cj.data
  (:refer-clojure :exclude [merge])
  (:require [com.cj.crmap :as crmap]
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
  (Data. (crmap/new actor) (crmap/new actor)))

(defn add-list [data list]
  {:pre (:id list)}
  (update :lists data crmap/add (:id list) list))

(defn get-list [data id]
  (crmap/get (:lists (query-dt data)) id))

(defn update-list [data id new-list]
  (update :lists data crmap/update id new-list))

(defn remove-list [data id]
  (update :lists data crmap/remove (get-list data id)))  

(defn update-list-with [data id fun & args]
  (update :lists data crmap/update id (apply fun (get-list id) args)))

(defn add-unique-item [data unique-item]
  {:pre (:id unique-item)}  
  (update :unique-items data crmap/add (:id unique-item) unique-item))

(defn get-unique-item [data id]
  (crmap/get (:unique-items (query-dt data)) id))

(defn update-unique-item [data id unique-item]
  (update :unique-items data crmap/update id unique-item))

(defn remove-unique-item [data id]
  (update :unique-items data crmap/remove (get-unique-item data id)))  

(defn merge [our their] (merge-dt our their))
