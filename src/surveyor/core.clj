(ns surveyor.core
  (:gen-class)
  (:require [clojure.pprint :as pprint])
  (:require [clj-time.core :as datetime])
  (:require [clj-time.format :as timeformat])
  (:require [clojure.string :as string])
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http]))

(load "aha")
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


(defn save-results-for-release
  [release]
  (let [features (map extract-custom (filter #(has-survey %)(get-features release)))
        surveys (group-by #(first (string/split (get % "survey") #"#")) features)]
    (doseq [[survey featurelist] surveys]
      (pprint/pprint {"responses_uri" (str survey "responses/")}))
    )
)

; MAIN PROGRAM FLOW

;(println (str "Please join the " "SBX-R-3" " feature survey at " (make-survey-for-release "SBX-R-3")))

; #5 Retrieve Results
;(save-results-for-release "SBX-R-3")

(strip-results (get-results {"responses_uri"
 "https://fluidsurveys.com/api/v3/surveys/717770/responses/"}))

; #6 Interpret Results
; #7 Save Results as Scores
