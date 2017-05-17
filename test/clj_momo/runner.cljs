(ns clj-momo.runner
  (:require [clj-momo.lib.clj-time.coerce-test]
            [clj-momo.lib.clj-time.core-test]
            [doo.runner :refer-macros [doo-tests]]))

(doo-tests 'clj-momo.lib.clj-time.coerce-test
           'clj-momo.lib.clj-time.core-test)
