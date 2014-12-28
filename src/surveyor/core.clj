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

;(-> (map extract-custom filtered) create-survey post-survey)



; MAIN PROGRAM FLOW
; #1 Select Release
(def filtered (take 5 (filter #(has-outcome %) (get-features "SBX-R-3"))))
(def features (map extract-custom filtered))
; #2 Create Survey
; #3 Save Survey to Features
(update-survey-urls (map extract-custom filtered) "https://fluidsurveys.com/api/v3/surveys/717704/")
; #4 Run Survey
; #5 Retrieve Results
(get-results {"responses_uri" "https://fluidsurveys.com/api/v3/surveys/717704/responses/"})
; #6 Interpret Results
; #7 Save Results as Scores
