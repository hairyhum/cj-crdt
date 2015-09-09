(ns com.cj.gset)

(use '[com.cj.crdt :only (StateCRDT)])

(defrecord G-Set [payload]
  StateCRDT
  (query-dt [gset] (:payload gset))
  (update-dt 
    [gset [action value]] 
      (case action
        ::add (update gset :payload conj value)
        ::remove (update gset :payload disj value)))
  (merge-dt [our their] (update our :payload clojure.set/union (:payload their))))

(defn new-gset
  ([] new-gset #{})
  ([set] (G-Set. set)))
