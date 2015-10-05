(ns com.cj.orswot
  (:refer-clojure :exclude [remove contains?])
  (:require [com.cj.vv :as vv])
  (:use [com.cj.crdt]))

; Data is map of value:dot
(defrecord Orswot [version data actor]
  StateCRDT
  (query-dt [orswot]
    (select-keys (:data orswot)))
  (update-dt [orswot [action value]]
    (case action
      :add 
        (let [actor (:actor orswot)
              new-version  (vv/increment (:version orswot) actor)
              item-dot (get actor new-version)
              new-data (assoc (:data orswot) value item-dot)]
          (assoc orswot :data new-data :version new-version))
      :remove
        (update orswot :data dissoc value)))
  (merge-dt [our their]
    (let [new-version (vv/merge (:version our) (:version their))
          our-data (reduce-kv (fn [acc key val]
                (let [their-val (get (:data their) key)]
                  (if their-val
                    (assoc acc key (vv/merge val their-val))
                    (if (vv/dot-greater? val (:version their))
                      (assoc acc key val)
                      acc)))))
          their-data (reduce-kv (fn [acc key val]
                  (if (get (:data our) key)
                    acc
                    (if (vv/dot-greater? val (:version our))
                      (assoc acc key val)
                      acc))))]
          (assoc our :data (merge our-data their-data) :version new-version))))

(defn new [actor] 
  (Orswot. (vv/new) {} actor))

(defn add [orswot value]
  (update-dt orswot [:add value]))

(defn remove [orswot value]
  (update-dt orswot [:remove value]))

(defn contains? [orswot value]
  (clojure.core/contains? (query-dt orswot) value))