(ns com.cj.vv
  (:refer-clojure :exclude [merge compare]))

(defn new ([map] map) ([] {}))

(defn summ-all [vv]
  (reduce-kv (fn [acc _ val] (+ acc val)) 0 vv))

(defn increment 
  ([vv actor val]
    (if (contains? vv actor)
      (update vv actor + val)
      (conj vv {actor val})))
  ([vv actor]
    (increment  vv actor 1)))

(defn merge [our their]
  (merge-with (fn [acc el] (max acc el)) our their))

(defn have-greater? [our their]
  (some (fn [[k v]] (> v (k their)))))

(defn ancestor? [our their]
  (and (have-greater? our their) (not (have-greater? their our))))

(defn child? [our their]
  (ancestor? their our))

(defn conflict? [our their]
  (and (have-greater? our their) (have-greater? their our)))

(defn compare [our their]
  (let [our-greater (have-greater? our their)
        their-grater (have-greater? their our)]
    (case [our-greater their-grater]
      [true true]   :conflict
      [true false]  :ancestor
      [false true]  :child
      [false false] :equal)))

(defn dot-greater? [our their]
  (ancestor? our (select-keys their (keys our))))