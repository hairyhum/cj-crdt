(ns com.cj.commands
  (:use [clojure.core.async]))

(def *commands* (atom {}))

(defn find-channel [key]
  (key (deref *commands*)))

(defn register-channel [key channel]
  (swap! *commands* conj {key channel}))

(defn unregister-channel [key]
  (swap! *commands* dissoc key))

(defn command [key & args]
  (let [channel (find-channel key)]
    (if channel
      (go (>! channel {key args})))))

(defn stop-channel [key]
  (let [channel (find-channel key)]
    (if channel
      (go (>! channel {:stop true})))))

(defn channel-reader [channel fun]
  (go-loop []
    (let [value (<! channel)]
      (if (and value (not (= :stopped (fun value)))) (recur)))))

(defn on-command [key fun]
  {:pre (find-channel key)}
  (let [channel (find-channel key)]
    (channel-reader 
      channel 
      (fn [command]
        (if (:stop command)
          :stopped
          (apply fun (get command key)))))))

(defn create-command [key fun]
  (register-channel key (chan))
  (on-command key fun))

(defn remove-command [key]
  (let [channel (find-channel key)]
    (if channel
      (do
        (close! channel)
        (unregister-channel key)))))
