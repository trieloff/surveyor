(ns surveyor.routes.fluidsurveys
  (:require [compojure.core :refer :all]
            [hiccup.element :refer :all]
            [hiccup.form :refer :all]
            [clojure.string :as string]
            [surveyor.config :refer [config]]
            [ring.util.response :refer [redirect]]
            [clj-oauth2.client :as oauth2]
            [surveyor.ring :as oauth2-ring]
            [surveyor.views.layout :as layout]))

;; :access-token-uri "https://fluidsurveys.com/accounts/oauth/token/"
;; :authentication-uri "https://fluidsurveys.com/accounts/developer/authorize/"


(def fluidsurveys-oauth2
  {:authorization-uri "https://fluidsurveys.com/accounts/developer/authorize/"
   :access-token-uri "https://fluidsurveys.com/accounts/oauth/token/"
   :redirect-uri (str "https://" ("aha.clientdomain") ":443/fluidsurveys.callback")
   :client-id (config "fluidsurveys.clientid")
   :client-secret (config "fluidsurveys.clientsecret")
   :access-query-param :access_token
   :grant-type "authorization_code"
   :force-https true
   :get-state oauth2-ring/get-state-from-session
   :put-state oauth2-ring/put-state-in-session
   :get-target oauth2-ring/get-target-from-session
   :put-target oauth2-ring/put-target-in-session
   :get-oauth2-data oauth2-ring/get-oauth2-data-from-session
   :put-oauth2-data oauth2-ring/put-oauth2-data-in-session
   :exclude #"^(/|/status.*|/css/.*)$"
   :trace-messages true})

(defn callback [request params]
  (str "<h1>Hello Callback</h1><code>"
       (str params)
       "<code>"
       "<code>"
       (str request)
       "</code>"))

(defroutes fluidsurveys-routes
  (GET "/fluidsurveys.login" {:keys [params] :as request} (callback request params)))
