(defproject juleffel/reframe-websocket "0.1.6"
  :description "Wraps basic reframe, websocket, and transit capabilities for communicating to a server."
  :url "https://github.com/ftravers/reframe-websocket"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/test.check "0.9.0"]
                 [re-frame "0.10.1"]
                 [org.clojure/core.async "0.3.443"]
                 [fentontravers/transit-websocket-client "0.4.11"]]

  :plugins [[lein-cljsbuild "1.1.7"  :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.14"]]

  :source-paths ["src/cljs" "src/cljc"]

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs" "src/cljc" "test/cljs"]
     :figwheel {:on-jsload "reframe-websocket.core/on-js-reload"
                :open-urls ["http://localhost:3449/index.html"]}
     :compiler {:main reframe-websocket.core
                :asset-path "js/compiled/out"
                :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
                :output-to "resources/public/js/compiled/rfws.js"
                :output-dir "resources/public/js/compiled/out"
                :source-map-timestamp true
                :preloads [devtools.preload day8.re-frame.trace.preload]}}]}
  
  :figwheel {:css-dirs ["resources/public/css"]}
  :profiles {:dev
             {:dependencies [[binaryage/devtools "0.9.4"]
                             [figwheel-sidecar "0.5.14"]
                             [com.cemerick/piggieback "0.2.2"]
                             [day8.re-frame/trace "0.1.11"]]
              :source-paths ["src/cljs" "src/cljc" "dev"]
              :plugins [[cider/cider-nrepl "0.16.0-SNAPSHOT"]]
              :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
              :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                :target-path]}})

