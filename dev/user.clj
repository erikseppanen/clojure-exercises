;;;; This is a dev namespace to contain utility functions useful for
;;;; interacting with the data at the REPL, or for general development.

(ns user
  (:require [clojure-exercises.data-analysis :refer :all]
            ;; [incanter.core :as ic :refer [view dim]]
            ;; [incanter.stats :as stats]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.repl :refer :all]
            [clojure.pprint :refer [pp pprint]]))
