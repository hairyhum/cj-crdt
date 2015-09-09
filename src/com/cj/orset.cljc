(ns com.cj.orset)

(use '[com.cj.crdt :only (StateCRDT)])

(defn get-id [val] (hash val))

(defrecord ORSet [values thumbstones]
  StateCRDT
  (query-dt [orset] (:values orset))
  (update-dt 
    [orset [action value]]
      (case action
        ::add 
          (let [id (get-id value)
                values (:values orset)
                thumbstones (:thumbstones orset)]
            (ORSet. (conj values {id value}) (conj thumbstones id)))
        ::remove 
          (let [id (get-id value)] 
            (update orset :values dissoc id))))
  (merge-dt [our their]
    (let [values (merge (:values our) (:values their))
          thumbstones (clojure.set/union (:thumbstones our) (:thumbstones their))]
      (ORSet. (dissoc values thumbstones) thumbstones))))

(defn new [] (ORSet. {} #{}))


  



