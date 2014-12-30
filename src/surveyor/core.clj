(ns surveyor.core
  (:gen-class)
  (:require [clojure.pprint :as pprint])
  (:require [clj-time.core :as datetime])
  (:require [clj-time.format :as timeformat])
  (:require [clojure.string :as string])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http]))

(load "aha")
(load "aggregates")
(load "fluidsurveys")

(def cli-options
  [["-s" "--survey RELEASE" "Create a new survey for a release"
    :validate [#(re-matches #"[A-Z]+-R-[0-9]+" %) "Must be a valid aha.io release ID"]]
   ["-u" "--update RELEASE" "Update tags and scores for a release"
    :validate [#(re-matches #"[A-Z]+-R-[0-9]+" %) "Must be a valid aha.io release ID"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        "Usage:"
        "  program-name -s SBX-R-1"
        "  …distribute survey and wait for responses…"
        "  program-name -s SBX-R-1"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  #_(System/exit status))

;(-main "-u" "SBX-R-3")

(defn make-survey-for-release
  [release]
  (let [filtered (filter #(has-outcome %) (get-features release))
        features (map extract-custom filtered)
        survey (post-survey (create-survey release features) release)]
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

(defn save-results-for-release
  [merged-results]
  (doseq [result merged-results]
    (update-tags (get result "feature") (get result "results"))
    (update-score (get result "feature") (get result "results"))
  )
)

; MAIN PROGRAM FLOW

;(println (str "Please join the " "SBX-R-3" " feature survey at " (make-survey-for-release "SBX-R-3")))

; #5 Retrieve Results
;(save-results-for-release (merge-results-for-release "SBX-R-3"))

; #6 Interpret Results
; #7 Save Results as Scores

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options
    (if (:survey options) ( println (str "Creating survey for " (:survey options) "\nPlease distribute the survey URL " (make-survey-for-release (:survey options)))))
    (if (:update options) ( pprint/pprint (str "Updating survey for " (:update options) " " (save-results-for-release (merge-results-for-release (:update options))))))
    ))

(-main "-s" "SBX-R-3")
