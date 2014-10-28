(in-ns 'surveyor.core)

(defn create-survey
  "Creates a JSON body for new surveys based on a list of features and
  outcomes"
  [features]
  (generate-string {"title" "My very fine survey"
                    "form" [{"children" [{"title" "Is that a question?"
                                          "idname" "text-response"
                                          "children" [{"type" "string"}]
                                          "id" "1"}]}]})
  ;)(generate-string features)
  )
