<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. Setup</a></li>
<li><a href="#sec-2">2. Reframe :set and :get event/subscription registration</a></li>
<li><a href="#sec-3">3. Send/Recv to Server</a>
<ul>
<li><a href="#sec-3-1">3.1. Define your endpoint</a></li>
<li><a href="#sec-3-2">3.2. Send to Server</a></li>
</ul>
</li>
<li><a href="#sec-4">4. Send/Recv to Server in a re-frame event</a></li>
<li><a href="#sec-5">5. Subscribing to server messages</a></li>
</ul>
</div>
</div>

This project use the cljs lib [ftravers/transit-websocket-client](https://github.com/ftravers/transit-websocket-client).
It should be paired with [ftravers/websocket-server](https://github.com/ftravers/websocket-server)

# Setup<a id="sec-1" name="sec-1"></a>

Add to project:

<a href="https://clojars.org/juleffel/reframe-websocket/" target="_blank">![Foo](https://clojars.org/juleffel/reframe-websocket/latest-version.svg)</a>

```clojure
    (ns ...
      (:require [reframe-websocket.core :as reframe-websocket]))

```

# Reframe :set and :get event/subscription registration<a id="sec-2" name="sec-2"></a>

This will create an event handler called `:get` and a subscription
handler called `:set` to be used like:

```clojure
    (reframe/dispatch-sync [:set [:some :path] "abc123"])
    ;; sets the path [:some :path] to value "abc123" in the app-db
    @(reframe/subscribe [:get [:some :path]])
    ;; => "abc123"

```

# Send/Recv to Server<a id="sec-3" name="sec-3"></a>

## Define your endpoint<a id="sec-3-1" name="sec-3-1"></a>

```clojure
    (def my-aws (reframe-websocket/async-websocket "ws://localhost:7890"))

```

## Send to Server<a id="sec-3-2" name="sec-3-2"></a>

```clojure
    ;; Send a message, specify where to store the response
    (let [my-message {:my-message "blah" :some-param 12345}
          my-store-location [:store :path]]
      (reframe-websocket/send-msg my-message my-store-location my-aws))        
    
    ;; retrieve the response
    @(reframe/subscribe [:get [:store :path]])

```

# Send/Recv to Server in a re-frame event<a id="sec-4" name="sec-4"></a>

## Define your endpoint<a id="sec-4-1" name="sec-4-1"></a>

```clojure
    (def my-aws (reframe-websocket/async-websocket "ws://localhost:7890"))

```

## Write an interceptor<a id="sec-4-2" name="sec-4-2"></a>

```clojure
    (defn ws-send-msg
      [path-msg path-resp db]
      (reframe-websocket/send-msg (get-in db path-msg) path-resp aws))

    (def path-msg [:send-msg :msg])
    (def path-resp [:send-msg :resp])
    (def ws-send-msg-interceptor (rf/after (partial ws-send-msg path-msg path-resp)))

```

## Add it to an event<a id="sec-4-3" name="sec-4-3"></a>

```clojure
    (rf/reg-event-db
      ::send-msg
      [ws-send-msg-interceptor]
      (fn-traced [db [_ msg]]
        (assoc-in db path-msg msg)))
```


# Subscribing to server message <a id="sec-5" name="sec-5"></a>

If you use [ftravers/websocket-server](https://github.com/ftravers/websocket-server) as the server websocket,
you should start your server with these input and output functions:

```clojure
    (start-ws-server
        port
        (fn [[store-path data]]
          [store-path (handle data)])
        (fn [s]
          (let [[_ rf-msg] (json/read-str s)]
            (read-string rf-msg)))
        (fn [msg]
          (json/write-str
            ["~#'" (str msg)])))
```

You can then send message from the backend with send-all. They will be stored in [:store :path] of your app-db
and trigger the subscribes.

```clojure
    ; String
    (send-all! port [[:store :path] "Message from backend"])])

    ; EDN
    (send-all! port [[:store :path] {:map "Hello" :text "EDN from backend"}])])

    ; You can subscribe to them as for responses to client requests:
    @(reframe/subscribe [:get [:store :path]])
```
