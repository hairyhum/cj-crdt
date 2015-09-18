(ns com.cj.orswot
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
            (update vset :versions assoc value (vv/new {actor 1})))
        :remove vset)))
  (merge-dt [ours theirs]
    (assoc ours :version (merge-with vv/merge (:versions ours) (:versions theirs)))))

(defrecord Orswot [structure data]
  StateCRDT
  (query-dt [orswot]
    (select-keys data (query-dt (:structure orswot))))
  (update-dt [orswot [action value]]
    {:pre (contains? value :id)}
    (case action
      :add 
        (let [id (:id value)
              new-structure (update-dt (:structure orswot) [:add id])
              new-data (assoc (:data orswot) id value)]
          (assoc orswot :data new-data :structure new-structure))
      :remove
        (let [id (:id value)
              new-structure (update-dt (:structure orswot) [:remove id])
              new-data (dissoc (:data orswot) id)]
          (assoc orswot :data new-data :structure new-structure))))
  (merge-dt [our their]
    (let [new-structure (merge-dt (:structure our) (:structure their))
          new-data (select-keys data (query-dt new-structure))]
      (Orswot. new-structure new-data))))

(defn new [actor] 
  (Orswot. (VersionSet. {} actor) {}))

(defn add [orswot value]
  {:pre (contains? value :id)}
  (update-dt orswot [:add value]))

(defn remove [orswot value]
  {:pre (contains? value :id)}
  (update-dt orswot [:remove value]))

(defn get [orswot key]
  (#'clojure.core/get (query-dt orswot) key))

(defn update [orswot key value]
  (update orswot :data assoc key value))