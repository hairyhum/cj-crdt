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

(create-data-command :sync (fn [old-data new-data] data/merge))

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

; (create-data-command :update-item-title 
;   (fn [data item title & o]
;     (let [list-id (:list-id item)
;           old-list (data/get-list data list-id)
;           new-list (list/update-item old-list (item/update-title item))]
;       (data/update-list data list-id new-list))))
