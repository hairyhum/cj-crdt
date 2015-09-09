(ns com.cj.crdt)

(defprotocol StateCRDT
  (query-dt [crdt] "Get value from CRDT")
  (update-dt [crdt params] "Update CRDT state")
  (merge-dt [our their] "Merge two CRDT replicas"))
