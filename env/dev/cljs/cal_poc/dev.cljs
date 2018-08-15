(ns ^:figwheel-no-load cal-poc.dev
  (:require
    [cal-poc.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
