(ns com.cj.item)

(deftype Item [id title amount]
  Object
  (equals [item other] (= (:id item) (:id other)))
  (hashCode [item] (hash (:id item))) 
  clojure.lang.IHashEq
  (hasheq [item] (hash (:id item))))  

(defn new [id title amount]
  (Item. id title amount))



