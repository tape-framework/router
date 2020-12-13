(ns tape.router-test
  (:require [clojure.set :as set]
            [cljs.test :refer [deftest testing is are async use-fixtures]]
            [day8.re-frame.test :as rft]
            [integrant.core :as ig]
            [tape.module :as module :include-macros true]
            [tape.mvc.controller :as c :include-macros true]
            [tape.mvc.view :as v]
            [tape.router :as router :include-macros true]))

(module/load-hierarchy)

(defn ^::c/event-fx foo [_ _] {})
(defn ^::c/event-fx bar [_ _] {})
(defn ^::c/event-fx baz [_ _] {})

(c/defmodule)

(def ^:private routes
  [["/foo" ::foo]
   ["/bar/:id" ::bar]
   ["/baz" ::baz]])

(def ^:private config
  {:tape.profile/base {::router/routes routes
                       :tape/router    {:routes  (ig/ref ::router/routes)
                                        :options {:use-fragment true
                                                  :conflicts    nil}}}
   ::c/module         nil
   ::v/module         nil
   ::router/module    nil
   ::module           nil})

(def ^:private system nil)

(use-fixtures :once
  {:before (fn [] (set! system (-> config module/prep-config ig/init)))
   :after  (fn [] (ig/halt! system))})

(deftest module-test
  (is (set/subset? #{:tape/router
                     ::router/href-fn
                     ::router/navigate-fx
                     ::router/navigate-event-fx}
                   (set (keys system)))))

(deftest href*-test
  (is (= "#/foo" (router/href* [::foo])))
  (is (= "#/bar/42" (router/href* [::bar {:id 42}]))))

(deftest navigate*-test
  (rft/run-test-async
   (router/navigate* [::baz])
   (rft/wait-for [::baz]
     (is (= "#/baz" (.. js/window -location -hash))))))

(deftest href-test
  (is (= "#/foo" (router/href [foo])))
  (is (= "#/bar/42" (router/href [bar {:id 42}]))))

(deftest navigate-test
  (rft/run-test-async
   (router/navigate [baz])
   (rft/wait-for [::baz]
     (is (= "#/baz" (.. js/window -location -hash))))))
