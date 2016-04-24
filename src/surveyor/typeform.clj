(ns surveyor.typeform
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http])
  (:require [surveyor.aggregates :refer :all])
  (:require [surveyor.config :refer :all])
  (:require [surveyor.kano :refer :all])
  (:require [clojure.string :as string])
  (:require [clojure.set :refer [intersection]])
  (:require [clojure.math.combinatorics :as combo])
  (:require [clojure.math.numeric-tower :as math])
  (:require [surveyor.config :refer :all])
  (:require [ranking-algorithms.core :refer [rank-glicko-teams]])
  (:require [aws.sdk.s3 :as s3]))

(defn create-question-nps [name]
  {:question (str "How likely is it that you would recommend " name " to a friend or colleague?")
   :type "opinion_scale"
   :labels {:left "Not likely" :right "Very likely"}
   :ref "nps"})

(defn create-question-freeform [name description]
  {:question description
   :type "long_text"
   :ref name})

(defn create-question-kano
  [id question outcomes]
  (map (fn [item] {:question question
          :description (get item "outcome")
          :type "multiple_choice"
          :choices (map #(identity {:label %}) ["I like it" "I expect it" "I don't care" "I can tolerate it" "I dislike it"])
          :ref (str id "-" (get item "reference_num"))}) outcomes))

(defn create-question-ulwick [id question outcomes]
  (map (fn [item] {:question question
                                 :description (get item "outcome")
                                 :type "rating"
                                 :shape "heart"
                                 :ref (str id "-" (get item "reference_num"))}) outcomes))

(defn conjoint-combinations [qualities]
  (let [samples (int (min 6 (max 1 (math/sqrt (count qualities)))))
        combis (combo/combinations (combo/combinations qualities samples) 2)]
    (take (min (* samples (count qualities)) (count combis)) (shuffle combis))))

(defn make-description [option-a option-b]
  (str
    "**Option A**  \n"
    (string/join "  \n" (map #(str " - " (get % "outcome")) option-a))
    "  \n**Option B**  \n"
    (string/join "  \n" (map #(str " - " (get % "outcome")) option-b))
    ))

(defn make-id [id option-a option-b]
  (str
    id
    "-"
    (string/join "-" (map #(get % "reference_num") option-a))
    "-vs-"
    (string/join "-" (map #(get % "reference_num") option-b))))

(defn create-conjoint-ulwick [id question features]
  (let [options (conjoint-combinations features)]
    (map (fn [option] {:question question
                       :type "multiple_choice"
                       :choices [{:label "Option A"} {:label "Option B"}]
                       :ref (make-id id (first option) (last option))
                       :description (make-description (first option) (last option))}) options)))

;; {:question question
;;             ;;:description (make-description (first %) (last %))
;;             ;;:ref (make-id id (first %) (last %))
;;             :type "multiple_choice"
;;             :choices [{:label "Option A"} {:label "Option B"}]}

(defn create-survey
  "Creates a JSON body for new surveys based on a list of features and
  outcomes"
  [release features name]
  (let [outcomes (map #(get % "outcome") features)
        survey {:title name
                :webhook_submit_url (str "https://6ruu1rr486.execute-api.us-east-1.amazonaws.com/dev/" release)
                :fields (concat [(create-question-nps name)]
                                 (create-conjoint-ulwick "ulwick-importance"
                                                         "Which of the two combinations of outcomes would be more important to you?"
                                                         features)
                                 [(create-question-freeform "importance-freeform" "Are there any other outcomes that would be highly important to you?")]
                                 (create-question-ulwick "ulwick-satisfaction"
                                                         "Considering the current state of things, how satisfied are you with the following outcome right now?"
                                                         features)
                                 [(create-question-freeform "satisfaction-freeform" "Are there any other outcomes you are either highly satisfied or unsatisfied with right now?")]
                                (create-question-kano "kano-positive"
                                                       "How would you feel if following outcome would be achieved?"
                                                       features)
                                (create-question-kano "kano-negative"
                                                       "How would you feel if following outcome are prevented, this means they cannot be achieved?"
                                                       features)
                                [(create-question-freeform "nps-booster" "If there was one single thing we could do to make you recommend the product, what would it be?")])}]

      (generate-string survey)))


(def creds {:access-key (config "aws.access.key") , :secret-key (config "aws.secret.key")})

(defn get-mapping [id mapping]
  (first (filter #(= id (:id %)) mapping)))


(def my-mapping (parse-string (slurp (:content (s3/get-object creds "tyepform" (str "SBX-R-5" "/mapping.json")))) true))

(get-mapping 4397302 my-mapping)

(defn simplify-answers [answers mapping]
  (map #(merge {:id (int (:field_id %))
                   :value (:amount (:value %) (:label (:value %) (:value %)))}
               (get-mapping (int (:field_id %)) mapping))
       answers))

(defn get-results [release]
  (let [mapping (parse-string (slurp (:content (s3/get-object creds "tyepform" (str release "/mapping.json")))) true)]
    (map #(simplify-answers % mapping)
       (map #(:answers (parse-string (slurp (:content (s3/get-object creds "tyepform" (:key %)))) true))
            (filter
              #(not (= (str release "/" "mapping.json") (:key %)))
              (:objects (s3/list-objects creds "tyepform" {:prefix release})))))))

(defn parse-importance [refstr]
  (let [sides (string/split (subs refstr 18) #"-vs-")]
    {:option-a (map #(string/join "-" %) (partition 2 (string/split (first sides) #"-")))
     :option-b (map #(string/join "-" %) (partition 2 (string/split (last  sides)  #"-")))}))

(defn get-field-mapping [json]
  (map #(identity {:id (:id %)
                   :ref (cond
                          (string/starts-with? (:ref %) "kano-") (subs (:ref %) 14)
                          (string/starts-with? (:ref %) "ulwick-satisfaction") (subs (:ref %) 20)
                          (string/starts-with? (:ref %) "ulwick-importance") (parse-importance (:ref %))
                          :else nil)
                   :type (cond
                           (string/starts-with? (:ref %) "ulwick-importance") "ulwick-importance"
                           (string/starts-with? (:ref %) "ulwick-satisfaction") "ulwick-satisfaction"
                           (string/starts-with? (:ref %) "kano-positive") "kano-positive"
                           (string/starts-with? (:ref %) "kano-negative") "kano-negative"
                           :else (:ref %))})
       (:fields json)))



(def typeform-options {:timeout 2000             ; ms
              :headers {"X-API-Token" (config "typeform.auth") "Content-Type" "application/json"}})

(defn post-survey
  ([json]
   (post-survey json "NULL"))
  ([json id]
  (let [{:keys [status headers body error] :as string}
        @(http/post "https://api.typeform.io/latest/forms" (assoc typeform-options :body json))]
    (let [parsed-body (parse-string body true)
          mapping (s3/put-object creds "tyepform" (str id "/mapping.json")
      (generate-string (get-field-mapping parsed-body)))]
      (surveyor.util/map-a-map first (group-by :rel (:_links parsed-body)))))))

(defn get-simple-answers [question results]
  (map :value (filter #(= question (:type %))
    (flatten results))))

(defn get-feature-answers [qtype results]
  (surveyor.util/map-a-map (fn [l] (map :value l))
    (surveyor.util/grouped-map-by :ref
      (map #(dissoc % :id :type)
          (filter #(= qtype (:type %))
            (flatten results))))))

(defn get-feature-combinations [qtype results]
  (mapcat identity
    (map #(combo/cartesian-product (:win %) (:lose %))
         (map #(case (:value %)
                 "Option A" {:win (-> % :ref :option-a) :lose (-> % :ref :option-b)}
                 "Option B" {:win (-> % :ref :option-b) :lose (-> % :ref :option-a)})
              (filter #(= qtype (:type %))
                      (flatten results))))))


(defn as-glicko-matches [matches]
  (map (fn [match] {:home (first match)
          :away (last match)
          :home_score 1
          :away_score 0}) matches))

(defn enrich-glicko-score [score]
  {:median (nth score 1)
   :stderr (last score)
   :val-max (+ (nth score 1) (* 2 (last score)))
   :val-min (- (nth score 1) (* 2 (last score)))})

(defn normalize-glicko-score [score max-val]
  {:median (/ (:median score) max-val)
   :stderr (/ (:stderr score) max-val)
   :val-max (/ (:val-max score) max-val)
   :val-min (/ (:val-min score) max-val)})

(defn group-glicko-scores ([question results]
  "Get the normalized Ulwick/Glicko importance for a set of questions"
  (let [scores (surveyor.util/map-a-map
                 enrich-glicko-score
                 (surveyor.util/map-a-map
                   first
                   (group-by
                     first
                     (rank-glicko-teams
                       (as-glicko-matches
                         (get-feature-combinations question results))))))
        max-val (apply max (vals (surveyor.util/map-a-map :val-max scores)))]
    (surveyor.util/map-a-map #(normalize-glicko-score % max-val) scores)))
  ([question results lookup]
  "Get the normalized Ulwick/Glicko importance for a set of questions, for specicic lookup key"
  (surveyor.util/map-a-map lookup (group-glicko-scores question results))))

(defn ulwick-score [importance satisfaction]
  (cond
    (and (number? importance) (number? satisfaction))
      (+ importance (max (- importance satisfaction) 0)) ;;single value calculation
    (and (map? importance) (map? satisfaction))
      (apply merge (map #(hash-map % (ulwick-score (get importance %) (get satisfaction %))) (intersection (set (keys satisfaction)) (set (keys importance)))))
    :else :default))


(def long-kano-matrix {
 ["I like it" "I like it"] "questionable" ["I like it" "I expect it"] "attractive"  ["I like it" "I don't care"] "attractive"  ["I like it" "I can tolerate it"] "attractive"  ["I like it" "I dislike it"] "one-dimensional"
 ["I expect it" "I like it"] "reverse"      ["I expect it" "I expect it"] "indifferent" ["I expect it" "I don't care"] "indifferent" ["I expect it" "I can tolerate it"] "indifferent" ["I expect it" "I dislike it"] "must-be"
 ["I don't care" "I like it"] "reverse"      ["I don't care" "I expect it"] "indifferent" ["I don't care" "I don't care"] "indifferent" ["I don't care" "I can tolerate it"] "indifferent" ["I don't care" "I dislike it"] "must-be"
 ["I can tolerate it" "I like it"] "reverse"      ["I can tolerate it" "I expect it"] "indifferent" ["I can tolerate it" "I don't care"] "indifferent" ["I can tolerate it" "I can tolerate it"] "indifferent" ["I can tolerate it" "I dislike it"] "must-be"
 ["I dislike it" "I like it"] "reverse"      ["I dislike it" "I expect it"] "reverse"     ["I dislike it" "I don't care"] "reverse"     ["I dislike it" "I can tolerate it"] "reverse"     ["I dislike it" "I dislike it"] "questionable"})

(defn kano-score [positive negative]
  (cond
    (and (string? positive) (string? negative)) (get long-kano-matrix [positive negative])
    (and (map? positive) (map? negative)) (apply merge (map #(hash-map % (kano-score (get positive %) (get negative %))) (intersection (set (keys positive)) (set (keys negative)))))
    (and (seq? positive) (seq? negative)) (kano-score (mode positive) (mode negative))))

(defn combined-results [results key]
  (let [kano (kano-score (get-feature-answers "kano-positive" my-results) (get-feature-answers "kano-negative" my-results))
        ulwick (ulwick-score (group-glicko-scores "ulwick-importance" my-results key) (aggregate-all-ulwick (get-feature-answers "ulwick-satisfaction" my-results) key))]
    (apply merge (map #(hash-map % {:kano (get kano %)
                                    :ulwick (Math/round (* 10 (get ulwick %)))})
         (intersection (set (keys kano)) (set (keys ulwick)))))))

(def my-results (get-results "SBX-R-5"))

(combined-results my-results :val-min)

(get-simple-answers "nps-booster" my-results)
(get-simple-answers "nps" my-results)
(aggregate-nps (get-simple-answers "nps" my-results))
(get-simple-answers "satisfaction-freeform" my-results)
(get-simple-answers "importance-freeform" my-results)
