(ns tape.router
  (:require [tape.mvc.meta :as meta]))

(defmacro href
  "Given the vector made of a named path qualified-symbol and a params map,
  returns a path string of that route with slots filled from params.
  Example: `(router/href [counter.c/increment])`."
  [[fsym & args]]
  `(href* ~(into [(meta/event-kw &env fsym)] args)))

(defmacro navigate
  "Given the vector made of a named path qualified-symbol and a params map,
  dispatches an event that navigates the browser to the path string of that
  route with slots filled from params. Example:
  `(router/navigate [counter.c/show])`."
  [[fsym & args]]
  `(navigate* ~(into [(meta/event-kw &env fsym)] args)))
