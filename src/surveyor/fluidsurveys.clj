(in-ns 'surveyor.core)

(def likert [ {"code" "like", "score" "", "label"
               {"en" "I like it"}}
              {"code" "must", "label"
               {"en" "I expect it"}}
              {"code" "neutral", "label"
               {"en" "I don't care"}}
              {"code" "tolerate", "label"
               {"en" "I can tolerate it"}}
              {"code" "dislike", "label"
               {"en" "I dislike it"}}])

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
