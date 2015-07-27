(ns surveyor.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cemerick.friend :as friend]
            [surveyor.workflow :as oauth2]
            [friend-oauth2.util     :refer [format-config-uri get-access-token-from-params extract-access-token]]
            [surveyor.config :refer [config]]
            [surveyor.ring :refer [wrap-oauth2]]
            [surveyor.routes.fluidsurveys :refer [fluidsurveys-routes fluidsurveys-oauth2]]
            [surveyor.routes.home :refer [home-routes]]))

(defn init []
  (println "surveyor is starting")
  (println oauth2/workflow {}))

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
                              :redirect_uri (format-config-uri client-config)}}})

(defn credential-fn
  "Upon successful authentication with the third party, Friend calls
  this function with the user's token. This function is responsible for
  translating that into a Friend identity map with at least the :identity
  and :roles keys. How you decide what roles to grant users is up to you;
  you could e.g. look them up in a database.

  You can also return nil here if you decide that the token provided
  is invalid. This could be used to implement e.g. banning users.

  This example code just automatically assigns anyone who has
  authenticated with the third party the nominal role of ::user."
  [token]
  (println "Habenus token: " token)
    {:identity token
     :roles #{:surveyor.handler/user}})

(def friend-config {:allow-anon? true
                    :unauthorized-handler surveyor.routes.home/unauthorized
                    :workflows   [(oauth2/workflow
                                   {:client-config client-config
                                    :uri-config uri-config
                                    :access-token-parsefn extract-access-token
;;                                    :config-auth config-auth
                                    :credential-fn credential-fn
                                    })
                                  ]})

(defn wrap-proxy [handler override]
  (fn [request]
    (if (or (get (:headers request) "x-forwarded-host")
            (get (:headers request) "X-Forwarded-For"))
      (handler (merge request override))
      (handler request))))

(def app
  (-> (routes home-routes fluidsurveys-routes app-routes)
      (wrap-oauth2 fluidsurveys-oauth2)
      (friend/authenticate friend-config)
      (wrap-base-url)
      (ring.middleware.session/wrap-session)
      (ring.middleware.keyword-params/wrap-keyword-params)
      (ring.middleware.params/wrap-params)
      (wrap-proxy {:server-port "443" :scheme :https})))


;; {:allow-anon? true
;;                             :default-landing-uri "/"
;;                             :login-uri "/aha.callback"
;;                             :unauthorized-handler surveyor.routes.home/unauthorized
;;                             :workflows [(oauth2/workflow
;;                                          {:client-config client-config
;;                                           :uri-config uri-config
;;                                           :access-token-parsefn get-access-token-from-params})]}
