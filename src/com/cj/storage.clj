(ns com.cj.storage
  (:refer-clojure :exclude [get remove])
  (:require [com.cj.vv :as vv])
  (:use [com.cj.crdt]))


(defrecord VersionSet [versions actor]
  StateCRDT
  (query-dt [vset] (keys (:versions vset)))
  (update-dt [vset [action value]]
    (let [version (#'clojure.core/get (:versions vset) value)]
      (case action
        :add
          (if version
            (update vset :versions assoc value (vv/increment version actor 1))
            (update vset :versions assoc value (vv/new actor 1)))
        :remove vset)))
  (merge-dt [ours theirs]
    (assoc ours :version (merge-with vv/merge (:versions ours) (:versions theirs)))))

(defrecord Storage [structure data]
  StateCRDT
  (query-dt [storage]
    (select-keys data (query-dt (:structure storage))))
  (update-dt [storage [action value]]
    {:pre (contains? value :id)}
    (case action
      :add 
        (let [id (:id value)
              new-structure (update-dt (:structure storage) [:add id])
              new-data (assoc (:data storage) id value)]
          (assoc storage :data new-data :structure new-structure))
      :remove
        (let [id (:id value)
              new-structure (update-dt (:structure storage) [:remove id])
              new-data (dissoc (:data storage) id)]
          (assoc storage :data new-data :structure new-structure))))
  (merge-dt [our their]
    (let [new-structure (merge-dt (:structure our) (:structure their))
          new-data (select-keys data (query-dt new-structure))]
      (Storage. new-structure new-data))))

(defn new [actor] 
  (Storage. (VersionSet. {} actor) {}))

(defn add [storage value]
  {:pre (contains? value :id)}
  (update-dt storage [:add value]))

(defn remove [storage value]
  {:pre (contains? value :id)}
  (update-dt storage [:remove value]))

(defn get [storage key]
  (#'clojure.core/get (query-dt storage) key))