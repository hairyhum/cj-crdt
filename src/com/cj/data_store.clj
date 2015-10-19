(ns com.cj.data-store
  (require [com.cj.data :as data]
           [com.cj.cached :as cached]
           [com.cj.commands :as commands]
           [com.cj.list :as list]
           [org.httpkit.client :as http-client]
           [clojure.data.json :as json])
  (use [clojure.core.async :only (go go-loop chan >! <! close! alt! timeout thread)]))

(defn log-agent-error [a exception]
  (println exception))

(def *data-store (agent (cached/new :actor) :error-handler log-agent-error))

(def *ui-channel (chan))

(add-watch *data-store :notify-ui 
  (fn [key reference old-state new-state]
    (go (>! *ui-channel (:working new-state)))))

(defn create-data-command [key fun]
  (commands/create-command key (fn [ & args]
    (let [fun-update (fn [cached] (cached/update cached fun args))]
      (send *data-store fun-update)))))

(commands/create-command :merge (fn [data]
  (send *data-store (fn [cached] (cached/merge cached data)))))

(def *sync-timeout 60)
(def *sync-server-url "localhost:8080")

(defn set-interval
  [f time-in-ms]
  (let [stop (chan)]
    (go-loop []
      (alt!
        (timeout time-in-ms) (do (<! (thread (f)))
                                 (recur))
        stop :stop))
    stop))

(defn async-put [data channel]
  (http-client/put
    *sync-server-url
    {:body data}
    (fn [{:keys [status headers body error]}]
      (go 
        (if error
          (do (println error) (close! channel))
          (>! channel :ok))))))

(defn async-get [channel]
  (http-client/get
    *sync-server-url
    (fn [{:keys [status headers body error]}]
      (go
        (if error
          (do (println error) (close! channel))
          (>! channel (json/read-json body)))))))

(defn sync-get []
  (go 
    (let [request-channel (chan 1)]
      (async-get request-channel)
      (commands/command :merge (<! request-channel)))))

(defn sync-put []
  (let [cached (deref *data-store)
        packed (:working cached/pack cached)]
    (if (not= packed (:base cached))
      (sync-put-request packed))))

(defn sync-put-request [packed]
  (go
    (let [request-channel (chan 1)]
      (async-put (json/json-str packed) request-channel)
      (if (= :ok (<! request-channel))
            (commands/commands :merge packed)))))

(def *sync-put-interval* nil)
(def *sync-get-interval* nil)

(defn start-sync []
  (set! *sync-put-interval* (set-interval sync-put *sync-timeout))
  (set! *sync-get-interval* (set-interval sync-get *sync-timeout)))

; (command :add-list list)
(create-data-command :add-list data/add-list)

; (command :remove-list list-id)
(create-data-command :remove-list data/remove-list)

; (command :add-unique-item unique-item)
(create-data-command :add-unique-item data/add-unique-item)

; (command :remove-unique-item unique-item-id)
(create-data-command :remove-unique-item 
  ; TODO: items delete
  data/remove-unique-item)


(create-data-command :update-list-name
  (fn [data list-id name]
    (data/update-list data list-id
      (list/update-name (data/get-list data list-id) name))))

(create-data-command :update-unique-item
  (fn [data unique-item]
    (data/update-unique-item data (:id unique-item) unique-item)))

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

