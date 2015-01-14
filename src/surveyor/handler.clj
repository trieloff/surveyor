(ns surveyor.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cemerick.friend :as friend]
            [friend-oauth2.workflow :as oauth2]
            [friend-oauth2.util     :refer [format-config-uri get-access-token-from-params]]
            [surveyor.core :refer [config]]
            [surveyor.routes.home :refer [home-routes]]))

(defn init []
  (println "surveyor is starting"))

(defn destroy []
  (println "surveyor is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def client-config
  {:client-id (config "aha.clientid")
   :client-secret (config "aha.clientsecret")
   ;; TODO get friend-oauth2 to support :context, :path-info
   :callback {:domain (str "https://" (config "aha.clientdomain")) :path "/aha.callback"}})

(def uri-config
  {:authentication-uri {:url (str "https://" (config "aha.host") ".aha.io" "/oauth/authorize")
                        :query {:client_id (:client-id client-config)
                                :response_type "code"
                                :redirect_uri (format-config-uri client-config)
                                :scope ""}}

   :access-token-uri {:url (str "https://" (config "aha.host") ".aha.io" "/oauth/token")
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (format-config-uri client-config)
                              :code ""}}})

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (friend/authenticate {:allow-anon? true
                            :default-landing-uri "/"
                            :login-uri "/aha.callback"
                            :unauthorized-handler surveyor.routes.home/unauthorized
                            :workflows [(oauth2/workflow
                                         {:client-config client-config
                                          :uri-config uri-config
                                          :access-token-parsefn get-access-token-from-params})]})
      (wrap-base-url)))
