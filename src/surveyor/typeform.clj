(ns surveyor.typeform
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http])
  (:require [surveyor.aggregates :refer :all])
  (:require [surveyor.config :refer :all]))

(defn create-question-nps [name]
  {:question (str "How likely is it that you would recommend " name " to a friend or colleague?")
   :type "opinion_scale"
   :labels {:left "Not likely" :right "Very likely"}
   :ref "nps"})

(defn create-question-freeform [name description]
  {:question description
   :type "long_text"
   :id name})

(defn create-survey
  "Creates a JSON body for new surveys based on a list of features and
  outcomes"
  [release features name]
  (generate-string {:title name
   :webhook_submit_url (str "https://6ruu1rr486.execute-api.us-east-1.amazonaws.com/dev/" release)
   :fields [(create-question-nps name)
            (create-question-ulwick "ulwick-importance"
;;                                     "In your work, how important are the following outcomes to you?"
;;                                     "least impotant" "most important" 0 10
;;                                     outcomes)
;;             (create-question-freeform "importance-freeform" "Are there any other outcomes that would be highly important to you?")
;;             (create-question-ulwick "ulwick-satisfaction"
;;                                     "Considering the current state of things, how satisfied are you with the following outcomes right now?"
;;                                     "not at all satisfied" "fully satisfied" 0 10
;;                                     outcomes)
;;             (create-question-freeform "satisfaction-freeform" "Are there any other outcomes you are either highly satisfied or unsatisfied with right now?")
;;             (create-question-kano "kano-posititive"
;;                                   "How would you feel if following outcome would be achieved?"
;;                                   outcomes)
;;             (create-question-kano "kano-negative"
;;                                   "How would you feel if following outcomes are prevented, this means they cannot be achieved?"
;;                                   outcomes)
            (create-question-freeform "nps-booster" "If there was one single thing we could do to make you recommend the product, what would it be?")]}))
