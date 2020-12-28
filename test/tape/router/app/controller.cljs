(ns tape.router.app.controller
  (:require [tape.mvc :as mvc :include-macros true]))

(def ^{::mvc/reg ::mvc/routes} routes
  [["/foo" ::foo]
   ["/bar/:id" ::bar]
   ["/baz" ::baz]])

(defn foo
  {::mvc/reg ::mvc/event-fx}
  [_ _] {})

(defn bar
  {::mvc/reg ::mvc/event-fx}
  [_ _] {})

(defn baz
  {::mvc/reg ::mvc/event-fx}
  [_ _] {})

(mvc/defm ::module)
