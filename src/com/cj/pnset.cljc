(ns com.cj.pnset
  (:refer-clojure :exclude [remove contains? count])
  (:use com.cj.crdt))


(defrecord PNSet [values deletes]
  StateCRDT
  (query-dt [pnset] (clojure.set/difference (:values pnset) (:deletes pnset)))
  (update-dt [pnset [action value]]
    (case action
      :add
        (update pnset :values conj value)
      :remove
        (let [new-values (disj (:values pnset) value)
              new-deletes (conj (:deletes pnset) value)]
          (assoc pnset :values new-values :deletes new-deletes))))
  (merge-dt [our their]
    (let [merged-deletes (clojure.set/union (:deletes our) (:deletes their))
          merged-values (clojure.set/difference
                          (clojure.set/union (:values our) (:values their))
                          merged-deletes)]
      (PNSet. merged-values merged-deletes))))

(defn new 
  ([] (PNSet. #{} #{}))
  ([set] (PNSet. set #{})))

(defn add
  [pnset value] (update-dt pnset [:add value]))

(defn remove
  [pnset value] (update-dt pnset [:remove value]))

(defn union
  [our their] (merge-dt our their))

(defn contains?
  [pnset value] (#'clojure.core/contains? (query-dt pnset) value))

(defn to-list
  [pnset] (seq (query-dt pnset)))

(defn count
  [pnset] (#'clojure.core/count (query-dt pnset)))

(defn to-set
  [pnset] (query-dt pnset))