(ns tape.router-test
  (:require [clojure.set :as set]
            [cljs.test :refer [deftest testing is are async use-fixtures]]
            [day8.re-frame.test :as rft]
            [integrant.core :as ig]
            [tape.module :as module :include-macros true]
            [tape.mvc :as mvc :include-macros true]
            [tape.router :as router :include-macros true]
            [tape.router.app.controller :as app.c]))

(module/load-hierarchy)

(def ^:private config
  {::mvc/module nil
   ::router/module nil
   ::app.c/module nil})

(deftest router-test
  (let [system (-> config module/prep-config ig/init)]
    (testing "module"
      (is (set/subset? #{:tape/router
                         ::router/href
                         ::router/navigate-fx
                         ::router/navigate-event-fx}
                       (set (keys system)))))

    (testing "href*"
      (is (= "#/foo" (router/href* [::app.c/foo])))
      (is (= "#/bar/42" (router/href* [::app.c/bar {:id 42}]))))

    (testing "navigate*"
      (rft/run-test-async
       (router/navigate* [::app.c/baz])
       (rft/wait-for [::app.c/baz]
         (is (= "#/baz" (.. js/window -location -hash))))))

    (testing "href"
      (is (= "#/foo" (router/href [app.c/foo])))
      (is (= "#/bar/42" (router/href [app.c/bar {:id 42}]))))

    (testing "navigate"
      (rft/run-test-async
       (router/navigate [app.c/baz])
       (rft/wait-for [::baz]
         (is (= "#/baz" (.. js/window -location -hash))))))

    (ig/halt! system)))
