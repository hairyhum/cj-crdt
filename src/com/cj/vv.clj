(ns com.cj.vv
  (:refer-clojure :exclude [merge]))

(defn new [map] map)

(defn summ-all [vv]
  (reduce-kv (fn [acc _ val] (+ acc val)) 0 vv))

(defn increment [vv actor val]
  (if (contains? vv actor)
    (update vv actor + val)
    (conj vv {actor val})))

(defn merge [our their]
  (merge-with (fn [acc el] (max acc el)) our their))
