(ns com.cj.list
  (:refer-clojure :exclude [name])
  (:require [com.cj.lwwregister :as register]
            [com.cj.pnset :as pnset]))

(defrecord List [name items bought])

(defn new
  ([] (#'com.cj.list/new ""))
  ([name] (List. (register/new name) (pnset/new) (pnset/new))))

(defn update-name
  [list name]
  (update list :name register/update name))

(defn name
  [list]
  (register/value (:name list)))

(defn items-count
  [list]
  (pnset/count (:items list)))

(defn bought-count
  [list]
  (pnset/count (:bought list)))

(defn not-bought-count
  [list]
  (- (items-count list) (bought-count list)))

(defn items-to-list
  [list]
  (pnset/to-list (:items list)))

(defn bought-to-list
  [list]
  (pnset/to-list (:bought list)))

(defn not-bought-to-list
  [list]
  (seq (clojure.set/difference 
          (pnset/to-set (:items list)) 
          (pnset/to-set (:bought list)))))

(defn contains-item?
  [list item]
  (pnset/contains? (:items list) item))

(defn contains-bought?
  [list item]
  (and (contains-item? list item) (pnset/contains? (:bought list) item)))

(defn contains-not-bought?
  [list item]
  (and (contains-item? list item) (not (contains-bought? list item))))

(defn add-item
  [list item]
  (update list :items pnset/add item))

(defn buy-item
  [list item]
  {:pre (contains-item? list item)}
  (update list :bought pnset/add item))

(defn remove-item
  [list item]
  {:pre (contains-item? list item)}
  (let [new-items  (pnset/remove (:items list) item)
        new-bought (pnset/remove (:bought list) item)]
    (assoc list 
      :items new-items
      :bought new-bought)))

(defn gen-id [] (rand-int 100000000))
(defn copy-item [item] (assoc item :id (gen-id)))

(defn unbuy-item
  [list item]
  {:pre (contains-bought? list item)}
  (add-item (remove-item list item) (copy-item item)))

(defn update-item
  [list item]
  {:pre (contains-item? list item)}
  (let [new-item (copy-item item)
        without-item (remove-item list item)
        with-item (add-item without-item new-item)]
    (if (contains-bought? list item)
      (buy-item with-item new-item)
      with-item)))


