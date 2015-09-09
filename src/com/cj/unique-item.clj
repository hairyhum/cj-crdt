(ns com.cj.unique-item
  (:require [com.cj.lwwregister :as register]
            [com.cj.maxcounter :as maxcounter]
            [com.cj.gcounter :as gcounter]
            [clj-time.core :as t]))


(defrecord UniqueItem [title permanent group-id use-count last-use])

(defn create [title {:keys [group-id permanent] :or {group-id 0 permanent false}}]
  (UniqueItem. 
    title 
    permanent 
    (register/new group-id) 
    (gcounter/new 0) 
    (maxcounter/new (t/date-time 1970 01 01))))

(defn update-group-id [uitem group-id]
  (update uitem :group-id register/update group-id))

(defn bump [uitem]
  (let [new-count (gcounter/inc (:use-count uitem) 1)
        new-use   (maxcounter/set (:last-use uitem) (t/now))]
    (assoc uitem :use-count new-count :last-use new-use)))


