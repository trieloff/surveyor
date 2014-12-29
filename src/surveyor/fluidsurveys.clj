(in-ns 'surveyor.core)

(def likert [ {"code" "like", "label"
               {"en" "I like it"}}
              {"code" "must", "label"
               {"en" "I expect it"}}
              {"code" "neutral", "label"
               {"en" "I don't care"}}
              {"code" "tolerate", "label"
               {"en" "I can tolerate it"}}
              {"code" "dislike", "label"
               {"en" "I dislike it"}}])

(def kano-matrix {
 [0 0] "questionable" [0 1] "attractive" [0 2] "attractive" [0 3] "attractive" [0 4] "one-dimensional"
 [1 0] "reverse" [1 1] "indifferent" [1 2] "indifferent" [1 3] "indifferent" [1 4] "must-be"
 [2 0] "reverse" [2 1] "indifferent" [2 2] "indifferent" [2 3] "indifferent" [2 4] "must-be"
 [3 0] "reverse" [3 1] "indifferent" [3 2] "indifferent" [3 3] "indifferent" [3 4] "must-be"
 [4 0] "reverse" [4 1] "reverse" [4 2] "reverse" [4 3] "reverse" [4 4] "questionable"
})

; bad, bad coder!
(def fluidsurveys-options {:timeout 2000             ; ms
              :headers {"Authorization" "Basic bGFyc0B0cmllbG9mZi5uZXQ6SDhwLVQ1Ui03YTYtellM" "Content-Type" "application/json"}})

(defn create-labels
  "Generates a sequence of labels for range responses."
  [from to]
  (map (fn [label] {"label" {"en" (str label)}}) (range from (inc to))))

(defn create-question-ulwick
  [id question least most from to outcomes]
  {"showBorders" false,
   "unique" false,
   "grouped" true,
   "idname" "single-choice-grid",
   "children" (map (fn [label] {"type" "single", "required" false, "static" false, "label" {"en" label}}) outcomes),
   "id" id,
   "alphabetize" false,
   "randomize" false,
   "randomizeLimit" false,
   "questionSize" 30,
   "uniqueError" "Responses must be unique",
   "title"
   {"en" question},
   "choices" (create-labels from to),
   "type" "question",
   "alternateBackground" false,
   "staticcol" false, "description"
   {"en" (str "Rate from " from " (" least ") to " to " (" most ")")}}
  )

(defn create-question-kano
  [id question outcomes]
  {"showBorders" false,
   "unique" false,
   "grouped" true,
   "idname" "dropdown-grid",
   "children" (map (fn [label] {"randomizeLimit" "", "alphabetize" false, "type" "dropdown", "required" false, "label" {"en" label}, "static" false, "randomize" false}) outcomes),
   "id" id,
   "alphabetize" false,
   "randomize" false,
   "randomizeLimit" false,
   "questionSize" 30,
   "uniqueError" "Responses must be unique",
   "title"
   {"en" question},
   "choices" likert,
   "type" "question",
   "alternateBackground" false,
   "staticcol" false,
   "description"
   {"en" ""}}
  )

(defn create-survey
  "Creates a JSON body for new surveys based on a list of features and
  outcomes"
  [features]
  (let [outcomes (map #(get % "outcome") features)]
  (generate-string
   {"structure" {"languages"
    [
     {"code" "en", "name" "English", "isDefault" true}],
    "form" [
            {"type" "page",
             "children" [
                         (create-question-ulwick "ulwick-importance"
                                                 "How import are the following outcomes to you?"
                                                 "least impotant" "most important" 0 10
                                                 outcomes)
                         (create-question-ulwick "ulwick-satisfaction"
                                                 "How satisfied are you with the following outcomes?"
                                                 "not at all satisfied" "fully satisfied" 0 10
                                                 outcomes)
                         (create-question-kano "kano-posititive"
                                               "How would you feel if following outcome would be achieved?"
                                               outcomes)
                         (create-question-kano "kano-negative"
                                               "How would you feel if following outcomes are prevented, this means they cannot be achieved?"
                                               outcomes)
                         ]}],
    "title" {"en" "Test Survey"}}}))
  ;)(generate-string features)
  )

;(group-by-feature (map kano-score (map ulwick-opportunity (strip-results (get-results {"responses_uri"
; "https://fluidsurveys.com/api/v3/surveys/717770/responses/"})))))

(defn map-a-map [f m]
  (into {} (for [[k v] m] [k (f v)])))

(def bla {"2" [["ulwick-importance_2" 6] ["ulwick-opportunity_2" 10] ["kano-negative_2" 2] ["kano-score_2" "indifferent"] ["kano-posititive_2" 2] ["ulwick-satisfaction_2" 2] ["ulwick-importance_2" 9] ["ulwick-opportunity_2" 10] ["kano-negative_2" 4] ["kano-score_2" "reverse"] ["kano-posititive_2" 1] ["ulwick-satisfaction_2" 8] ["ulwick-importance_2" 9] ["ulwick-opportunity_2" 16] ["kano-negative_2" 4] ["kano-score_2" "reverse"] ["kano-posititive_2" 0] ["ulwick-satisfaction_2" 2]], "3" [["ulwick-importance_3" 3] ["ulwick-opportunity_3" 3] ["kano-negative_3" 3] ["kano-posititive_3" 2] ["kano-score_3" "indifferent"] ["ulwick-satisfaction_3" 4] ["ulwick-importance_3" 9] ["ulwick-opportunity_3" 14] ["kano-negative_3" 4] ["kano-posititive_3" 0] ["kano-score_3" "reverse"] ["ulwick-satisfaction_3" 4] ["ulwick-importance_3" 8] ["ulwick-opportunity_3" 13] ["kano-negative_3" 4] ["kano-posititive_3" 1] ["kano-score_3" "reverse"] ["ulwick-satisfaction_3" 3]], "1" [["ulwick-importance_1" 5] ["kano-score_1" "reverse"] ["kano-negative_1" 4] ["ulwick-opportunity_1" 6] ["ulwick-satisfaction_1" 4] ["kano-posititive_1" 1] ["ulwick-importance_1" 1] ["kano-score_1" "indifferent"] ["kano-negative_1" 2] ["ulwick-opportunity_1" 1] ["ulwick-satisfaction_1" 6] ["kano-posititive_1" 2] ["ulwick-importance_1" 4] ["kano-score_1" "reverse"] ["kano-negative_1" 2] ["ulwick-opportunity_1" 5] ["ulwick-satisfaction_1" 3] ["kano-posititive_1" 0]], "0" [["ulwick-satisfaction_0" 3] ["kano-posititive_0" 0] ["kano-negative_0" 4] ["ulwick-opportunity_0" 11] ["kano-score_0" "reverse"] ["ulwick-importance_0" 7] ["ulwick-satisfaction_0" 6] ["kano-posititive_0" 2] ["kano-negative_0" 2] ["ulwick-opportunity_0" 1] ["kano-score_0" "indifferent"] ["ulwick-importance_0" 1] ["ulwick-satisfaction_0" 6] ["kano-posititive_0" 0] ["kano-negative_0" 2] ["ulwick-opportunity_0" 3] ["kano-score_0" "reverse"] ["ulwick-importance_0" 3]]})

(group-by-question bla)

(contains? ["questionable" "attractive" "indifferent" "reverse" "must-be"] "indifferent")



(defn remove-labels
  [labeled]
  (filter #(or (number? %) (contains? (set (vals kano-matrix)) %)) (flatten labeled))
)

(defn group-by-question
  [featuremap]
  (map-a-map (fn
               [results]
               (map-a-map
                remove-labels
                (group-by (fn
                           [el]
                           (string/replace (first el) #"_.*" "")) results))) featuremap)
)

(defn group-by-feature
  [results]
  (group-by (fn [el] (string/replace (first el) #".*_" ""))
            (mapcat identity (map
                    (fn [el] (seq el))
                    results)))
)

(defn calculate-kano-score
  [positive negative]
  (kano-matrix (vector negative positive))
)

(re-find #"kano-posititive_[0-9]+" "kano-posititive_1")

(defn kano-score
  [result]
  (reduce-kv
   (fn [mymap key value]
     (if (re-find #"kano-posititive_[0-9]+" key)
       (assoc mymap
         (string/replace key "-posititive_" "-score_")
         (calculate-kano-score value (get mymap (string/replace key "-posititive_" "-negative_"))))
       mymap))
   result result)
)

(defn calculate-ulwick-opportunity
  [importance satisfaction]
  (+ importance (max (- importance satisfaction) 0))
)

(defn ulwick-opportunity
  [result]
  (reduce-kv
   (fn [mymap key value]
     (if (re-find #"ulwick-importance_[0-9]+" key)
       (assoc mymap
         (string/replace key "-importance_" "-opportunity_")
         (calculate-ulwick-opportunity value (get mymap (string/replace key "-importance_" "-satisfaction_"))))
       mymap))
   result result)
)

(defn filter-result-keys
  [result key value]
  (if (re-find #"[0-9]+$" key)
    (assoc result key value)
    result
    )
)

(defn strip-result
  [result]
  (reduce-kv filter-result-keys {} result)
)

(defn strip-results
  [responses]
  (map strip-result responses)
)

(defn post-survey
  ([json]
   (post-survey json "Feature Survey"))
  ([json title]
  (let [{:keys [status headers body error] :as string}
        @(http/post "https://fluidsurveys.com/api/v3/surveys/" (assoc fluidsurveys-options :form-params {:name title}))]
    (let [survey (parse-string body)]
      (let [{:keys [status headers body error] :as string}
            @(http/put (get survey "survey_structure_uri") (assoc fluidsurveys-options :body json))] survey)
      ))))

(defn get-results
  [survey]
  (let [{:keys [status headers body error] :as string}
    @(http/get (get survey "responses_uri") fluidsurveys-options)]
    (let [response (parse-string body)
          results (get response "results")]
      (if-not (= nil (get response "next"))
        (lazy-seq (into results (get-results {"responses_uri" (get response "next")})))
        results)
    )
))
