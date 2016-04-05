(ns surveyor.fluidsurveys
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http])
  (:require [surveyor.aggregates :refer :all])
  (:require [surveyor.kano :refer :all])
  (:require [surveyor.config :refer :all]))

(def fluidsurveys-options {:timeout 2000             ; ms
              :headers {"Authorization" (str "Basic " (config "fluidsurveys.auth")) "Content-Type" "application/json"}})

(defn fs-token-options [token]
  (assoc-in fluidsurveys-options [:headers "Authorization"] (str "Bearer " token)))

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

(defn create-question-nps [name]
  {"description" {"en" ""}
   "title" {"en" (str "How likely is it that you would recommend " name " to a friend or colleague?")}
   "idname" "net-promoter"
   "children" [{"leftLabel" {"en" "Very Likely"}, "required" false, "showCatLabels" false, "rightLabel" {"en" "Not Likely"}, "flipOrder" true, "type" "drill-down", "display" "likert", "categories" [{"label" {"en" "Promoter"}, "choices" [{"score" 10, "label" {"en" "10"}} {"score" 9, "label" {"en" "9"}}]} {"label" {"en" "Passive"}, "choices" [{"score" 8, "label" {"en" "8"}} {"score" 7, "label" {"en" "7"}}]} {"label" {"en" "Detractor"}, "choices" [{"score" 6, "label" {"en" "6"}} {"score" 5, "label" {"en" "5"}} {"score" 4, "label" {"en" "4"}} {"score" 3, "label" {"en" "3"}} {"score" 2, "label" {"en" "2"}} {"score" 1, "label" {"en" "1"}} {"score" 0, "label" {"en" "0"}}]}]}]
   "grouped" true
   "type" "question"
   "id" "nps"}
)

;;   {
;;                     "description": {
;;                         "en": "Are there other outcomes that would be highly important to you?"
;;                     },
;;                     "title": {
;;                         "en": "Comments"
;;                     },
;;                     "idname": "text-response",
;;                     "children": [
;;                         {
;;                             "type": "string",
;;                             "has_cols": false,
;;                             "required": false,
;;                             "appearance": "full",
;;                             "cols": ""
;;                         }
;;                     ],
;;                     "type": "question",
;;                     "id": "lWQlR73l40"
;;                 }


(def ideas-question { "description" {"en" "If you have additional ideas, please submit them to our <a rel=\"nofollow\" href=\"https://blue-yonder.ideas.aha.io\">Blue Yonder Ideas Portal</a>."}
                      "title" {"en" "More Ideas"}
                      "idname" "section-separator"
                      "children" []
                      "type" "question"
                      "id" "ideas"})

(defn create-question-freeform [name description]
  {"description" {"en" description}
   "title" {"en" "Comments"}
   "idname" "text-response"
   "children" [{"type" "string" "has_cols" false "required" false "appearance" "full" "cols" ""}]
   "type" "question"
   "id" name}
)

(defn create-survey
  "Creates a JSON body for new surveys based on a list of features and
  outcomes"
  [release features name]
  (let [outcomes (map #(get % "outcome") features)]
  (generate-string
   {"structure" {"languages"
    [
     {"code" "en", "name" "English", "isDefault" true}],
    "form" [
            {"type" "page",
             "children" [(create-question-nps name)
                         (create-question-ulwick "ulwick-importance"
                                                 "In your work, how important are the following outcomes to you?"
                                                 "least impotant" "most important" 0 10
                                                 outcomes)
                         (create-question-freeform "importance-freeform" "Are there any other outcomes that would be highly important to you?")
                         (create-question-ulwick "ulwick-satisfaction"
                                                 "Considering the current state of things, how satisfied are you with the following outcomes right now?"
                                                 "not at all satisfied" "fully satisfied" 0 10
                                                 outcomes)
                         (create-question-freeform "satisfaction-freeform" "Are there any other outcomes you are either highly satisfied or unsatisfied with right now?")
                         (create-question-kano "kano-posititive"
                                               "How would you feel if following outcome would be achieved?"
                                               outcomes)
                         (create-question-kano "kano-negative"
                                               "How would you feel if following outcomes are prevented, this means they cannot be achieved?"
                                               outcomes)
                         (create-question-freeform "nps-booster" "If there was one single thing we could do to make you recommend the product, what would it be?")
                         ideas-question]}],
    "title" {"en" (str "Survey for release " release)}}}))
  ;)(generate-string features)
  )

;(group-by-feature (map kano-score (map ulwick-opportunity (strip-results (get-results {"responses_uri"
; "https://fluidsurveys.com/api/v3/surveys/717770/responses/"})))))

(defn map-a-map [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn remove-labels
  [labeled]
  (filter #(or (number? %) (contains? (set (vals kano-matrix)) %)) (flatten labeled))
)


(defn filter-notnil [el]
  (filter (fn [el] (not (nil? el))) el))


(defn aggregate-scores [el]
  (let [cleanel (filter-notnil el)]
    (if (every? integer? cleanel)
      (int (mean cleanel))
      (mode cleanel))))

(defn aggregate-results
  [featuremap]
  (map-a-map (fn [results] (map-a-map aggregate-scores results)) featuremap)
)

(defn group-by-question
  [featuremap]
  (map-a-map (fn
               [results]
               (map-a-map
                remove-labels
                (group-by (fn
                           [el]
                           (clojure.string/replace (first el) #"_.*" "")) results))) featuremap)
)

(defn group-by-feature
  [results]
  (group-by (fn [el] (clojure.string/replace (first el) #".*_" ""))
            (mapcat identity (map
                    (fn [el] (seq el))
                    results)))
)

(defn calculate-kano-score
  [positive negative]
  (kano-matrix (vector positive negative))
)

(re-find #"kano-posititive_[0-9]+" "kano-posititive_1")

(defn kano-score
  [result]
  (reduce-kv
   (fn [mymap key value]
     (if (re-find #"kano-posititive_[0-9]+" key)
       (assoc mymap
         (clojure.string/replace key "-posititive_" "-score_")
         (calculate-kano-score value (get mymap (clojure.string/replace key "-posititive_" "-negative_"))))
       mymap))
   result result)
)

(defn calculate-ulwick-opportunity
  [importance satisfaction]
  (if (or (nil? importance) (nil? satisfaction)) nil
  (+ importance (max (- importance satisfaction) 0))))

(defn ulwick-opportunity
  [result]
  (reduce-kv
   (fn [mymap key value]
     (if (re-find #"ulwick-importance_[0-9]+" key)
       (assoc mymap
         (clojure.string/replace key "-importance_" "-opportunity_")
         (calculate-ulwick-opportunity value (get mymap (clojure.string/replace key "-importance_" "-satisfaction_"))))
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
  ([json fstoken]
   (post-survey json "Feature Survey" fstoken))
  ([json title fstoken]
  (let [{:keys [status headers body error] :as string}
        @(http/post "https://fluidsurveys.com/api/v3/surveys/" (assoc (fs-token-options fstoken) :form-params {:name title}))]
    (let [survey (parse-string body)]
      (let [{:keys [status headers body error] :as string}
            @(http/put (get survey "survey_structure_uri") (assoc (fs-token-options fstoken) :body json))] survey)
      ))))

(defn get-results
  [survey fstoken]
  (let [{:keys [status headers body error] :as string}
    @(http/get (get survey "responses_uri") (fs-token-options fstoken))]
    (let [response (parse-string body)
          results (get response "results")]
      (if-not (= nil (get response "next"))
        (lazy-seq (into results (get-results {"responses_uri" (get response "next")} fstoken)))
        results)
    )
))
