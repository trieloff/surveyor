(ns surveyor.typeform
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http])
  (:require [surveyor.aggregates :refer :all])
  (:require [surveyor.config :refer :all])
  (:require [surveyor.kano :refer :all])
  (:require [clojure.string :as string])
  (:require [clojure.math.combinatorics :as combo])
  (:require [clojure.math.numeric-tower :as math])
  (:require [surveyor.config :refer :all])
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

(defn simplify-answers [answers]
  (map #(identity {:id (int (:field_id %))
                   :value (:amount (:value %) (:label (:value %) (:value %)))})
       answers))

(defn get-results [release]
  (map simplify-answers
    (map #(:answers (parse-string (slurp (:content (s3/get-object creds "tyepform" (:key %)))) true))
       (:objects (s3/list-objects creds "tyepform" {:prefix release})))))


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

(get-results "SBX-R-5")


