(ns surveyor.typeform
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http])
  (:require [surveyor.aggregates :refer :all])
  (:require [surveyor.config :refer :all]))

(defn create-survey
  "Creates a JSON body for new surveys based on a list of features and
  outcomes"
  [release features name]
  (identity features))
