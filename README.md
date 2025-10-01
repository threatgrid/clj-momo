# clj-momo

This clojure library can be seen as a common library for many `threatgrid` projects.
It centralizes many generic functions.

## Changes

- 0.4.1
  - Dependency upgrades:
    - clojure: 1.10.1 → 1.12.3
    - tools.logging: 0.5.0 → 1.3.0
    - prismatic/schema: 1.1.12 → 1.4.1
    - metosin/schema-tools: 0.12.2 → 0.13.1
    - cheshire: 5.9.0 → 6.1.0
    - clj-http: 3.12.3 → 3.13.1
    - riemann-clojure-client: 0.5.1 → 0.5.4
    - error_prone_annotations: 2.1.3 → 2.42.0
    - clojurescript: 1.10.597 → 1.12.42
    - logback-classic: 1.2.3 → 1.5.19
  - Switch from PhantomJS to Node.js for ClojureScript tests
  - Fix `goog.date.UtcDateTime` comparison support for Node.js

- 0.4.0
  - upgrades clj-http 3.12.3 which deprecates `json-strict` operations

## License

Copyright © 2017-2020 Cisco

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
