(ns examples.login.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [cljs.core.async :as async :refer [put! chan]]
              [om-local.core :as local]))

(enable-console-print!)

(defonce app-state (atom {:auth {:email "" :pass ""}}))

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

(defn main-component [data owner]
  (om/component
   (om/build login (:auth data))))

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
