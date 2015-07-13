(ns surveyor.routes.fluidsurveys
  (:require [compojure.core :refer :all]
            [hiccup.element :refer :all]
            [hiccup.form :refer :all]
            [clojure.string :as string]
            [cemerick.friend :as friend]
            [surveyor.aha :as aha]
            [surveyor.core :as core]
            [oauth.client :as oauth]
            [surveyor.views.layout :as layout]))


(defn login []
  "Hello World")

(defroutes fluidsurveys-routes
  (GET "/fluidsurveys.login" [] (login)))
