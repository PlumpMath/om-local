(defproject om-local "0.1.1-SNAPSHOT"
  :description "Sync a cursor in Om's state into localStorage or sessionStorage"
  :url "https://github.com/bensu/om-local"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-RC2"]
                 [org.clojure/clojurescript "0.0-3308" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [hodgepodge "0.1.3"]
                 [bensu/om "0.8.9-SNAPSHOT"]]

  :scm {:name "git"
        :url "https://github.com/bensu/om-local"}

  :deploy-repositories [["clojars" {:creds :gpg}]]

  :plugins [[lein-cljsbuild "1.0.4"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["examples/login/out"]
  
  :cljsbuild {:builds [{:id "login"
                        :source-paths ["src" "examples/login/src"]
                        :compiler {:output-to "examples/login/out/main.js"
                                   :output-dir "examples/login/out"
                                   :optimizations :none
                                   :main examples.login.core
                                   :asset-path "out"
                                   :source-map true
                                   :source-map-timestamp true
                                   :cache-analysis true}}]})
