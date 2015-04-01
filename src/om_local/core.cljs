(ns ^:figwheel-always om-local.core
    (:require-macros [cljs.core.async.macros :refer [go-loop go]])
    (:require [om.core :as om :include-macros true]
              [cljs.core.async :as async :refer [put! chan]]
              [hodgepodge.core :refer [get-item set-item 
                                       local-storage session-storage]]
              [om.dom :as dom :include-macros true]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:auth {:email ""
                                 :pass ""}}))

(defn login [data owner]
  (om/component
   (dom/div nil
            (dom/input 
             #js {:onChange #(om/update! data :email (.. % -target -value) ::local)
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
                             (om/update! data [] {:email "" :pass ""} ::local))}
             "Clear!"))))

(defn main-component [data owner]
  (om/component
   (om/build login (:auth data))))

;; API

(defn- to-indexed
  "Makes sure the cursor-path is a []"
  [cursor-path] 
  {:pre [(or (vector? cursor-path) true)]}  ;; FIX: add (atomic?)
  (if (vector? cursor-path)
    cursor-path
    [cursor-path]))

(def debug-on? (atom false))

(defn print-log [& args]
  (if @debug-on?
    (apply println args)))

;; FIX: The data is properly set on the first try, the problem is that
;; the render order
(defn om-local [data owner opts]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [local-path (to-indexed (:local-path opts))
            local-index (pr-str local-path)
            tx-chan (om/get-shared owner :tx-chan)
            txs (chan)]
        (if (:debug opts) (reset! debug-on? true))
        (async/sub tx-chan :txs txs)
        (om/set-state! owner :txs txs)
        (if-let [init-data (::local local-storage)]
          ;; FIX this HACK
          ;; since we update! during will-mount, no re-renders are triggered
          (go (let [_ (<! (async/timeout 100))]
                (om/update! data local-path init-data))))
        (go-loop [] 
          (let [[{:keys [new-state tag]} _] (<! txs)]
            (print-log "Got tag:" tag)
            (when (= ::local tag)
              (let [state (get-in new-state local-path)]
                (print-log "Got state:" state)
                (assoc! local-storage ::local state)))
            (recur)))))
    om/IRender
    (render [_]
      (om/build (:view-component opts) data {:opts (:opts opts)}))))

(let [tx-chan (chan)
      tx-pub-chan (async/pub tx-chan (fn [_] :txs))]
  (om/root
   (fn [data owner]
     (reify
       om/IRender
       (render [_]
         (om/build om-local data
                   {:opts {:view-component main-component 
                           :debug true
                           :local-path [:auth :email]}}))))
   app-state
   {:target (. js/document (getElementById "app"))
    :shared {:tx-chan tx-pub-chan}
    :tx-listen (fn [tx-data root-cursor]
                 (put! tx-chan [tx-data root-cursor]))}))
