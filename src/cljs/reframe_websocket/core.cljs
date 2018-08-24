(ns reframe-websocket.core
  (:require [re-frame.core :as rf]
            [cljs.reader :as reader]
            [transit-websocket-client.core :as websocket]
            [cljs.core.async :refer [<! >! offer!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn recv-msgs [aws]
  (go-loop
    []
    (let [[store-path msg] (reader/read-string (<! aws))]
      (rf/dispatch [:set store-path msg])
      (recur))))

(defn async-websocket [ws-url]
  (let [aws (websocket/async-websocket ws-url)]
    (recv-msgs aws)
    aws))

(rf/reg-event-db
 :set
 (fn [db [_ keys value]]
   (assoc-in db keys value)))

(rf/reg-sub
 :get
 (fn [db [_ keys]]
   (get-in db keys)))

(defn send-msg [msg store-path aws]
  "Send msg to server, storing the response in store-path."
  (offer! aws (str [store-path msg])))

(comment
  (let [my-message {:my-message "blah" :some-param 12345}
        my-store-location [:store :path]
        my-aws (async-websocket "ws://localhost:8899")]
    (send-msg my-message my-store-location my-aws)))
