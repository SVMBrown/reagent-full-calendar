(ns cal-poc.prod
  (:require [cal-poc.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
