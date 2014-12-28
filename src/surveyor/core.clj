(ns surveyor.core
  (:gen-class)
  (:require [clojure.pprint :as pprint])
  (:require [clj-time.core :as datetime])
  (:require [clj-time.format :as timeformat])
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http]))

(load "aha")
(load "fluidsurveys")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def filtered (take 3 (filter #(has-outcome %) (get-features "BYP-R-1"))))

(map extract-outcome filtered)

(-> (map extract-outcome filtered) create-survey post-survey)
