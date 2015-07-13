(ns surveyor.routes.fluidsurveys
  (:require [compojure.core :refer :all]
            [hiccup.element :refer :all]
            [hiccup.form :refer :all]
            [clojure.string :as string]
            [surveyor.config :refer [config]]
            [ring.util.response :refer [redirect]]
            [clj-oauth2.client :as oauth2]
            [surveyor.views.layout :as layout]))

;; :access-token-uri "https://fluidsurveys.com/accounts/oauth/token/"
;; :authentication-uri "https://fluidsurveys.com/accounts/developer/authorize/"


(def fluidsurveys-oauth2
  {:authorization-uri "https://fluidsurveys.com/accounts/developer/authorize/"
   :access-token-uri "https://fluidsurveys.com/accounts/oauth/token/"
   :redirect-uri "https://localhost/fluidsurveys.callback"
   :client-id (config "fluidsurveys.clientid")
   :client-secret (config "fluidsurveys.clientsecret")
   :access-query-param :access_token
   :grant-type "authorization_code"})

(oauth2/make-auth-request fluidsurveys-oauth2 "some-csrf-protection-string")
(defn login []
  (let [auth-req (oauth2/make-auth-request fluidsurveys-oauth2 "some-csrf-protection-string")]
    (redirect (:uri auth-req))))

(defn callback [request params]
  (str "<h1>Hello Callback</h1><code>"
       (str params)
       "<code>"
       "<p>"
       (oauth2/get-access-token fluidsurveys-oauth2 params)
       "</p>"))

(defroutes fluidsurveys-routes
  (GET "/fluidsurveys.login" [] (login))
  (GET "/fluidsurveys.callback" {:keys [params] :as request} (callback request params)))
