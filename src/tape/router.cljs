(ns tape.router
  (:refer-clojure :exclude [rest])
  (:require [integrant.core :as ig]
            [reagent.ratom :as ratom]
            [re-frame.core :as rf]
            [reitit.frontend :as rfr]
            [reitit.frontend.history :as rfh]
            [tape.module :as module]
            [tape.mvc.controller :as c]))

;;; Helpers

;; We disguise Delay as a Reaction to use the subscription mechanism
;; for function calling. Used in hrefs.
(extend-type Delay
  ratom/IDisposable
  (dispose! [this]
    (when-some [a (.-on-dispose-arr this)]
      (dotimes [i (alength a)]
        ((aget a i) this))))
  (add-on-dispose! [this f]
    (if-some [a (.-on-dispose-arr this)]
      (.push a f)
      (set! (.-on-dispose-arr this) (array f)))))

;;; Interface

(defn href*
  "Given the vector `to` made of a named path qualified-keyword and a params
  map, returns a path string of that route with slots filled from params.
  Example: `(router/href* [::counter.c/increment])`."
  [to]
  @(rf/subscribe [::href to]))

(defn navigate*
  "Given the vector `to` made of a named path qualified-keyword and a params
  map, dispatches an event that navigates the browser to the path string of
  that route with slots filled from params. Example:
  `(router/navigate* [::counter.c/show])`."
  [to]
  (rf/dispatch [::navigate to]))

;;; Re-Frame

(defmethod ig/init-key ::navigate-fx [_ {:keys [history]}]
  ;; Workaround for: https://ask.clojure.org/index.php/8975
  (let [go-fx ^{::c/fx ::navigate} (fn [to] (apply rfh/push-state history to))]
    go-fx))

(defn- ^{::c/event-fx ::navigate} navigate-event-fx [_ [_ to]]
  {::navigate to})

(defmethod ig/init-key ::href [_ {:keys [history]}]
  (fn [_db [_ to]] (delay (apply rfh/href history to))))

;;; Integrant

(defmethod ig/init-key ::routes [_ routes]
  (into [""] routes))

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

;;; Module

(defmethod ig/prep-key ::module [_ config]
  (assoc config ::requires {:controller (ig/ref ::c/module)}))

(defmethod ig/init-key ::module [_ _]
  (fn [config]
    (module/merge-configs
     config {::routes            (ig/refset ::c/routes)
             ::options           {:conflicts nil}           ;; can be provided via profile
             :tape/router        {:routes  (ig/ref ::routes)
                                  :options (ig/ref ::options)}
             ::href              (ig/ref :tape/router)
             ::navigate-fx       (ig/ref :tape/router)
             ::navigate-event-fx #'navigate-event-fx})))
