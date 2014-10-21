(ns surveyor.core
  (:gen-class)
  (:require [clojure.pprint :as pprint])
  (:require [clj-time.core :as datetime])
  (:require [clj-time.format :as timeformat])
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http]))

(load "aha")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def filtered (take 3 (filter #(has-outcome %) (get-features "BYP-R-1"))))

(map (fn [feature] (hash-map
                    "reference_num" (get feature "reference_num")
                    "resource" (get feature "resource")
                    "url" (get feature "url")
                    "name" (get feature "name")
                    "outcome" (get (first (filter
                               #(= (get % "key") "outcome")
                               (get feature "custom_fields"))) "value")
                    )) filtered)

