# om-local

-- Work in progress --

## Installing

[![Clojars Project](http://clojars.org/om-routes/latest-version.svg)](http://clojars.org/om-routes)

    (:require [om-routes.core :as routes])

## Description

Syncs a cursor in Om's app-state to localState/sessionState in the
browser giving you automatic persistent memory across sessions. It
helps for example by replacing [GarlicJS](http://garlicjs.org/) and
allowing you to add similar behavior on data that is not form-related.

Follows same structure as
[om-sync](https://github.com/swannodette/om-sync) and
[om-routes](https://github.com/bensu/om-routes).

It amounts to very little code and is probably not *exactly* what you
need, yet I find it a useful pattern and worth considering.

```clj
(defonce app-state (atom {:auth {:email "" :pass ""}}))

(defn main-component
    "Submits login with email and password"
    [data owner]
    ...)

(let [tx-chan (chan)
      tx-pub-chan (async/pub tx-chan (fn [_] :txs))]
  (om/root
   (fn [data owner]
     (reify
       om/IRender
       (render [_]
         (om/build local/om-local data
                   {:opts {:view-component main-component 
                           :debug true
                           :local-path [:auth :email]}}))))
   app-state
   {:target (. js/document (getElementById "app"))
    :shared {:tx-chan tx-pub-chan}
    :tx-listen (fn [tx-data root-cursor]
                 (put! tx-chan [tx-data root-cursor]))}))
```

## Minimal Example

We are going to code a simple widget that allows the user to enter
his/her email and password. We want to persist the email across
sessions. We can start from a template that includes Om, core.async, and
[Figwheel](https://github.com/bhauman/lein-figwheel) for easier
development:

    lein new figwheel local-example -- --om
    cd local-example

We start by adding `om-routes` to `project.clj`:

```clj
:dependencies [[org.clojure/clojure "1.6.0"]
               [org.clojure/clojurescript "0.0-3126"]
               [figwheel "0.2.5"]
               [org.clojure/core.async "0.1.346.0-17112a-alpha"]
               [sablono "0.3.4"]
               [org.omcljs/om "0.8.8"]
               [om-local "0.1.1-SNAPSHOT"]] ;; <- Add this
```

Then by editing `src/local-example/core.cljs` and adding some
requirements:

```clj
(ns routes-example.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.core.async :as async :refer [put! chan]]
        	  [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [om-local.core :as routes]))
```

Next we can set the structure of the state: 

```clj
(defonce app-state (atom {:auth {:email "" :pass ""}}))
```

Then we write the `:view-component` that will take and display data:

```clj
(defn login [data owner]
  (om/component
   (dom/div nil
            (dom/input 
             #js {:onChange #(om/update! data :email (.. % -target -value) 
                                         :om-local.core/local)
                  :type "email"
                  :value (:email data)
                  :placeholder "Email"})
            (dom/input 
             #js {:onChange #(om/update! data :pass (.. % -target -value))
                  :type "password"
                  :value (:pass data)
                  :placeholder "Password"})
            (dom/button 
             #js {:onClick (fn [_] 
                             (om/update! data [] {:email "" :pass ""} 
                                         :om-local.core/local))}
             "Clear!"))))
```

Note that we are tagging every `update!` to the state we want to persist with
a namespace qualified keyword, `:om-local.core/local`. This definitions 

Then we set up a pub-channel for all the transactions. `om-local`
will listen to those tagged with `:om-local.core/local`. Finally, we
wrap the `view-component` with `om-local` by passing it along `:local-path` 

```clj
(let [tx-chan (chan)
      tx-pub-chan (async/pub tx-chan (fn [_] :txs))]
  (om/root
   (fn [data owner]
     (reify
       om/IRender
       (render [_]
         (om/build local/om-local data
                   {:opts {:view-component main-component 
                           :debug true
                           :local-path [:auth :email]}}))))
   app-state
   {:target (. js/document (getElementById "app"))
    :shared {:tx-chan tx-pub-chan}
    :tx-listen (fn [tx-data root-cursor]
                 (put! tx-chan [tx-data root-cursor]))}))
```

Notice adding the `:debug` option, which defaults to `false`.

## Run the Examples

    git clone https://github.com/bensu/om-local
    cd om-local
    lein cljsbuild once login 

Open `examples/login/index.html` in your browser.

## Contributions

Pull requests, issues, and feedback welcome.

## License

Copyright © 2015 Sebastian Bensusan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
