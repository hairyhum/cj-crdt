(ns com.cj.cached
  (:refer-clojure :exclude [update])
  (require [com.cj.data :as data]))

(defn new [actor]
  (let [base (data/new actor)]
    {:base base :working base}))

(defn merge [cached data]
  (let [base (data/merge (:base cached) data)
        working (data/merge (:working cached) base)]
    {:base base :working working}))

(defn update [cached fun args]
  (clojure.core/update cached :working fun args))

(defn pack [cached]
  (clojure.core/update cached :working data/pack (:base cached)))

