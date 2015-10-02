(ns com.cj.data-store
  (require [com.cj.data :as data]
           [com.cj.commands :as commands]
           [org.httpkit.client :as http-client])
  (use [clojure.core.async]))

(defn log-agent-error [a exception]
  (println exception))

(def *data-store* (agent (data/new :actor) :error-handler log-agent-error))

(def *ui-channel* (chan))

(add-watch *data-store* :notify-ui 
  (fn [key reference old-state new-state]
    (go (>! *ui-channel* new-state))))

(defn create-data-command [key fun]
  (commands/create-command key (fn [ & args]
    (apply send *data-store* fun args))))

(def *sync-timeout* 60)
(def *sync-server-url* "localhost:8080")

(create-data-command :sync data/merge)

(defn set-interval
  [f time-in-ms]
  (let [stop (chan)]
    (go-loop []
      (alt!
        (timeout time-in-ms) (do (<! (thread (f)))
                                 (recur))
        stop :stop))
    stop))

(defn sync-server []
  (go
    (let [request-channel (chan 1)]
      (async-put (json/json-str (deref *data-store*)) request-channel)
      (if (= :ok (<! request-channel))
        (do
          (async-get request-channel)
          (command :sync (<! request-channel)))))))

(defn async-put [data channel]
  (http-client/put
    *sync-server-url*
    {:body data}
    (fn [{:keys [status headers body error]}]
      (go 
        (if error
          (do (println error) (close! channel))
          (>! channel :ok))))))

(defn async-get [channel]
  (http-client/get
    *sync-server-url*
    (fn [{:keys [status headers body error]}]
      (go
        (if error
          (do (println error) (close! channel))
          (>! channel (json/read-json body)))))))

(def *sync-server-interval* (set-interval sync-server *sync-timeout*))

(create-data-command :add-list data/add-list)

(create-data-command :remove-list data/remove-list)

(create-data-command :add-unique-item data/add-unique-item)

(create-data-command :remove-unique-item 
  ; TODO: items delete
  data/remove-unique-item)

(create-data-command :update-list-name
  (fn [data list-id name]
    (data/update-list data list-id
      (list/update-name (data/get-list data list-id) name))))

(create-data-command :update-unique-item
  (fn [data unique-item]
    (data update-unique-item data (:id unique-item) unique-item)))

(create-data-command :add-unique-item
  ; TODO: add unique item
  (fn [data list-id item]
    (let [list (data/get-list data list-id)]
      (data/update-list data list-id (list/add-item item)))))

(create-data-command :update-item
  (fn [data list-id item]
    (data/update-list-with data list-id list/update-item item)))

(create-data-command :remove-item
  (fn [data list-id item]
    (data/update-list-with data list-id list/remove-item item)))

(create-data-command :add-item
  ; TODO: add unique item
  (fn [data list-id item]
    (data/update-list-with data list-id list/add-item item)))

