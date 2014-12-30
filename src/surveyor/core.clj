(ns surveyor.core
  (:gen-class)
  (:require [clojure.pprint :as pprint])
  (:require [clj-time.core :as datetime])
  (:require [clj-time.format :as timeformat])
  (:require [clojure.string :as string])
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http]))

(load "aha")
(load "aggregates")
(load "fluidsurveys")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn make-survey-for-release
  [release]
  (let [filtered (filter #(has-outcome %) (get-features release))
        features (map extract-custom filtered)
        survey (-> features create-survey post-survey)]
    (do
      (update-survey-urls (map extract-custom filtered) (get survey "survey_uri"))
      (get survey "deploy_url")))
)


(defn merge-results-for-release
  [release]
  (apply concat (let [features (map extract-custom (filter #(has-survey %)(get-features release)))
        surveys (group-by #(first (string/split (get % "survey") #"#")) features)]
    (for [[survey featurelist] surveys]
      (let [survey-results (aggregate-results
                      (group-by-question
                       (group-by-feature
                        (map
                         kano-score (map
                                     ulwick-opportunity
                                     (strip-results
                                      (get-results {"responses_uri" (str survey "responses/")})))))))]
        (map #(hash-map
               "feature" (get % "reference_num")
               "survey" (first (string/split (get % "survey") #"#"))
               "response" (last (string/split (get % "survey") #"#"))
               "results" (get survey-results (last (string/split (get % "survey") #"#")))) featurelist)))
    ))
)

; MAIN PROGRAM FLOW

;(println (str "Please join the " "SBX-R-3" " feature survey at " (make-survey-for-release "SBX-R-3")))

; #5 Retrieve Results
(merge-results-for-release "SBX-R-3")

; #6 Interpret Results
; #7 Save Results as Scores
