(defproject om-local "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [hodgepodge "0.1.3"]
                 [org.omcljs/om "0.8.8"]]

  :plugins [[lein-cljsbuild "1.0.4"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["examples/login/out"]
  
  :cljsbuild {
              :builds [{:id "login"
                        :source-paths ["src" "examples/login/src"]
                        :compiler {:output-to "examples/login/out/main.js"
                                   :output-dir "examples/login/out"
                                   :optimizations :none
                                   :main examples.login.core
                                   :asset-path "out"
                                   :source-map true
                                   :source-map-timestamp true
                                   :cache-analysis true}}]})
