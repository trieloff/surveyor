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

(def filtered (take 3 (filter #(has-outcome %) (get-features "SBX-R-3"))))

(map extract-custom filtered)

;(-> (map extract-custom filtered) create-survey post-survey)

(update-survey-urls (map extract-custom filtered) "https://fluidsurveys.com/api/v3/surveys/717704/")

(get-results {"responses_uri" "https://fluidsurveys.com/api/v3/surveys/717704/responses/"})

; MAIN PROGRAM FLOW
; #1 Select Release
; #2 Create Survey
; #3 Save Survey to Features
; #4 Run Survey
; #5 Retrieve Results
; #6 Interpret Results
; #7 Save Results as Scores
