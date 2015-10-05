(ns com.cj.crmap
  (:refer-clojure :exclude [remove get update])
  (:require [com.cj.orswot :as orswot])
  (:use [com.cj.crdt]))

(defrecord CRMap [structure data]
  StateCRDT
  (query-dt [crmap] (:data crmap))
  (update-dt 
    [crmap [action & args]]
      (case action
        :add 
          (let [[key val] args
                new-structure (update-dt (:structure crmap) [:add key])
                new-data (assoc (:data crmap) key val)]
            (assoc crmap :data new-data :structure new-structure))
        :remove
          (let [[key] args
                new-structure (update-dt (:structure crmap) [:remove key])
                new-data (dissoc (:data crmap) key)]
            (assoc crmap :data new-data :structure new-structure))))
  (merge-dt [our their]
    (let [new-structure (merge-dt (:structure our) (:structure their))
          new-data (select-keys (merge (:data our) (:data their)) (query-dt new-structure))]
      (assoc our :structure structure :data new-data))))


(defn new [actor] (CRMap. (orswot/new actor) {}))

(defn add [crmap id value] (update-dt crmap [:add id value]))

(defn remove [crmap id] (update-dt crmap [:remove id]))

(defn get [crmap id] (id (query-dt crmap)))

; Clean update value only. Error if not added yet.
(defn update [crmap id new-value]
  {:pre (id crmap)}
  (clojure.core/update crmap :data assoc id new-value))