## README

STATUS: Pre-alpha, in design and prototyping phase.

#### About

`tape.router`

Integrates the [reitit](https://github.com/metosin/reitit) router via a
`tape.module` module, and provides a few handy ways to navigate.
The router is optional and can be left out in apps that don't need one.

#### Usage

You must be familiar with [reitit](https://github.com/metosin/reitit),
`tape.module` and `tape.mvc` before proceeding.

##### Install

Add `tape.router` to your deps:

```clojure
tape/router {:local/root "../router"}
```

##### Module & routes

In `config.edn` add `:tape.router/module` and define routes:

```edn
{:tape.profile/base 
 {:tape.router/routes [["/hi/:say" :sample.app.greet.controller/hi]]
  :tape/router {:routes #ig/reg :tape.router/routes
                :options {:use-fragment true}}}

 :tape.router/module nil
 ...}
```

A route resolves to a namespaced keyword that gets dispatched on route match:
`[::greet.c/hi parameters]`. The `parameters` has a `:path` map with params
matched in the path, and a `:query` map with params matched in the query. The
keyword is also the name of the route.

##### Navigate

```cljs
(ns sample.app.my-ns
  (:require [tape.router :as router]
            [sample.app.greet.controller :as greet.c]))
```

To navigate we use the route name and a params map that fills the route params
slots; we have the following utilities:

- `(router/href [::greet.c/hi {:say "Hi!"}])` returns the path `::greet.c/hi`
  with path params filled from the provided map `m`. Is used in hiccup anchor
  hrefs: `[:a {:href (router/href ...)}]`.
- `(router/navigate [::greet.c/hi {:say "Hi!"}])` to navigate to a route, used
  in DOM event handlers (on-click etc).
- `[::router/navigate [::greet.c/hi {:say "Hi!"}]]` a Re-Frame event to that
  can be dispatched to navigate to a route.
- `{::router/navigate [::greet.c/hi {:say "Hi!"}]}` a Re-Frame effect to that
  results in the browser navigating to a route.

#### License

Copyright © 2019 clyfe

Distributed under the MIT license.