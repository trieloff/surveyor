(in-ns 'surveyor.core)


{"languages"
 [
  {"code" "en", "name" "English", "isDefault" true}],
 "form" [
         {"type" "page",
          "children" [
                      {"showBorders" false,
                       "unique" false,
                       "grouped" true,
                       "idname" "single-choice-grid",
                       "children" [
                                   {"type" "single", "required" false, "static" false, "label"
                                    {"en" "My dog runs fast"}}
                                   {"type" "single", "required" false, "static" false, "label"
                                    {"en" "My dog barks loudly"}}],
                       "id" "MXG7uITKA2",
                       "alphabetize" false,
                       "randomize" false,
                       "randomizeLimit" false,
                       "questionSize" 30,
                       "uniqueError" "Responses must be unique",
                       "title"
                       {"en" "How important are the following outcomes for you?"},
                       "choices" [
                                  {"label"
                                   {"en" "0"}}
                                  {"label"
                                   {"en" "1"}}
                                  {"label"
                                   {"en" "2"}}
                                  {"label"
                                   {"en" "3"}}
                                  {"label"
                                   {"en" "4"}}
                                  {"label"
                                   {"en" "5"}}
                                  {"label"
                                   {"en" "6"}}
                                  {"label"
                                   {"en" "7"}}
                                  {"label"
                                   {"en" "8"}}
                                  {"label"
                                   {"en" "9"}}
                                  {"label"
                                   {"en" "10"}}],
                       "type" "question",
                       "alternateBackground" false,
                       "staticcol" false, "description"
                       {"en" "Rate from 0 (not important) to 10 (most important)"}}
                      {"showBorders" false,
                       "unique" false,
                       "grouped" true,
                       "idname" "single-choice-grid",
                       "children" [
                                   {"type" "single", "required" false, "static" false, "label"
                                    {"en" "Maximize the speed of my dog"}}
                                   {"type" "single", "required" false, "static" false, "label"
                                    {"en" "Maximize the volume of my dog's barks"}}],
                       "id" "34Ucx2UbmL",
                       "alphabetize" false,
                       "randomize" false,
                       "randomizeLimit" false,
                       "questionSize" 30,
                       "uniqueError"
                       "Responses must be unique", "title"
                       {"en" "How satisfied are you with following outcomes"},
                       "choices" [
                                  {"label"
                                   {"en" "0"}}
                                  {"label"
                                   {"en" "1"}}
                                  {"label"
                                   {"en" "2"}}
                                  {"label"
                                   {"en" "3"}}
                                  {"label"
                                   {"en" "4"}}
                                  {"label"
                                   {"en" "5"}}
                                  {"label"
                                   {"en" "6"}}
                                  {"label"
                                   {"en" "7"}}
                                  {"label"
                                   {"en" "8"}}
                                  {"label"
                                   {"en" "9"}}
                                  {"label"
                                   {"en" "10"}}],
                       "type" "question",
                       "alternateBackground" false,
                       "staticcol" false,
                       "description"
                       {"en" "Rate from 0 (not at all satisfied) to 10 (fully satisfied)"}}
                      {"showBorders" false,
                       "unique" false,
                       "grouped" true,
                       "idname" "dropdown-grid",
                       "children" [
                                   {"randomizeLimit" "", "alphabetize" false, "type" "dropdown", "required" false, "label"
                                    {"en" "My dog runs fast"}, "static" false, "randomize" false}
                                   {"randomizeLimit" "", "alphabetize" false, "type" "dropdown", "required" false, "label"
                                    {"en" "My dog barks loudly"}, "static" false, "randomize" false}],
                       "id" "T50nWmv9nd",
                       "alphabetize" false,
                       "randomize" false,
                       "randomizeLimit" false,
                       "questionSize" 30,
                       "uniqueError" "Responses must be unique",
                       "title"
                       {"en" "How would you feel if following outcomes can be achieved."}, "choices" [
                                                                                                      {"code" "like", "score" "", "label"
                                                                                                       {"en" "I like it"}}
                                                                                                      {"code" "must", "label"
                                                                                                       {"en" "I expect it"}}
                                                                                                      {"code" "neutral", "label"
                                                                                                       {"en" "I don't care"}}
                                                                                                      {"code" "tolerate", "label"
                                                                                                       {"en" "I can tolerate it"}}
                                                                                                      {"code" "dislike", "label"
                                                                                                       {"en" "I dislike it"}}], "type" "question", "alternateBackground" false, "staticcol" false, "description"
                       {"en" ""}}
                      {"showBorders" false,
                       "unique" false,
                       "grouped" true,
                       "idname" "dropdown-grid",
                       "children" [
                                   {"randomizeLimit" "", "alphabetize" false, "type" "dropdown", "required" false, "label"
                                    {"en" "My dog runs fast"}, "static" false, "randomize" false}
                                   {"randomizeLimit" "", "alphabetize" false, "type" "dropdown", "required" false, "label"
                                    {"en" "My dog barks loudly"},
                                    "static" false, "randomize" false}],
                       "id" "eXlWMuDymb",
                       "alphabetize" false,
                       "randomize" false,
                       "randomizeLimit" false,
                       "questionSize" 30,
                       "uniqueError" "Responses must be unique",
                       "title"
                       {"en" "How would you feel if following outcomes are prevented, this means they cannot be achieved."},
                       "choices" [
                                  {"code" "like", "label"
                                   {"en" "I like it"}}
                                  {"code" "must", "label"
                                   {"en" "I expect it"}}
                                  {"code" "neutral", "label"
                                   {"en" "I don't care"}}
                                  {"code" "tolerate", "label"
                                   {"en" "I can tolerate it"}}
                                  {"code" "dislike", "label"
                                   {"en" "I dislike it"}}],
                       "type" "question",
                       "alternateBackground" false,
                       "staticcol" false,
                       "description"
                       {"en" ""}}]}], "title"
 {"en" "Test Survey"}}

(defn create-labels
  "Generates a sequence of labels for range responses."
  [from to]
  (map (fn [label] {"label" {"en" (str label)}}) (range from (inc to))))

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
