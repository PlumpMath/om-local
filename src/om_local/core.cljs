(ns om-local.core
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put! chan]]
            [hodgepodge.core :refer [get-item set-item 
                                     local-storage session-storage]]))

;; ======================================================================  
;; Helpers 

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

;; ======================================================================  
;; API

(defn om-local [data owner opts]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [local-path (to-indexed (:local-path opts))
            local-index (pr-str local-path)
            tx-chan (om/get-shared owner :tx-chan)
            txs (chan)]
        (when (:debug opts)
          (reset! debug-on? true))
        (async/sub tx-chan :txs txs)
        (om/set-state! owner :txs txs)
        (when-let [init-data (::local local-storage)]
          (om/update! data local-path init-data))
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
