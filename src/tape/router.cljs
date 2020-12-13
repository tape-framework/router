(ns tape.router
  (:refer-clojure :exclude [rest])
  (:require [integrant.core :as ig]
            [re-frame.core :as rf]
            [reitit.frontend :as rfr]
            [reitit.frontend.history :as rfh]
            [tape.module :as module]
            [tape.mvc.controller.reg-fn :as reg-fn]
            [tape.mvc.controller :as c]))

;;; Interface

(defn href*
  "Given the vector `to` made of a named path qualified-keyword and a params
  map, returns a path string of that route with slots filled from params.
  Example: `(router/href* [::counter.c/increment])`."
  [to]
  (reg-fn/subscribe [::href to]))

(defn navigate*
  "Given the vector `to` made of a named path qualified-keyword and a params
  map, dispatches an event that navigates the browser to the path string of
  that route with slots filled from params. Example:
  `(router/navigate* [::counter.c/show])`."
  [to]
  (rf/dispatch [::navigate to]))

;;; Re-Frame

(defn- ^{::c/event-fx ::navigate} navigate-event-fx [_ [_ to]]
  {::navigate to})

;;; Integrant

(defn- on-navigate [match]
  (let [{:keys [data parameters]} match
        {:keys [name]} data]
    (when name
      (rf/dispatch [name parameters]))))

(defmethod ig/init-key :tape/router [_ {:keys [routes options]}]
  (let [router-options (dissoc options :use-fragment)
        start-options  (select-keys options [:use-fragment])
        router         (rfr/router routes router-options)
        history        (rfh/start! router on-navigate start-options)]
    {:router  router
     :history history}))

(defmethod ig/halt-key! :tape/router [_ {:keys [history]}]
  (rfh/stop! history))

(defmethod ig/init-key ::navigate-fx [_ {:keys [history]}]
  ;; Workaround for: https://ask.clojure.org/index.php/8975
  (let [go-fx ^{::c/fx ::navigate} (fn [to] (apply rfh/push-state history to))]
    go-fx))

(defmethod ig/init-key ::href-fn [_ {:keys [history]}]
  ;; Workaround for: https://ask.clojure.org/index.php/8975
  (let [href-fn ^{::c/reg-fn ::href} (fn [to] (apply rfh/href history to))]
    href-fn))

;;; Module

(defmethod ig/prep-key ::module [_ config]
  (assoc config ::requires {:controller (ig/ref ::c/module)}))

(defmethod ig/init-key ::module [_ _]
  (fn [config]
    (module/merge-configs
     config {::routes            nil                        ;; provided
             :tape/router        nil                        ;; provided
             ::href-fn           (ig/ref :tape/router)
             ::navigate-fx       (ig/ref :tape/router)
             ::navigate-event-fx #'navigate-event-fx})))
