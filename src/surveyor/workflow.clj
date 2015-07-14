(ns surveyor.workflow
  (:require
   [friend-oauth2.util :as util]
   [cemerick.friend :as friend]
   [clojure.pprint :as pprint]
   [ring.middleware.params :as ring-params]
   [clj-http.client :as client]
   [ring.util.request :as request]))

(defn- default-credential-fn
  [creds]
  {:identity (:access-token creds)})

(defn- is-oauth2-callback?
  [config request]
  (or (= (request/path-info request)
         (get-in config [:client-config :callback :path]))
      (= (request/path-info request)
         (or (:login-uri config) (-> request ::friend/auth-config :login-uri)))))

(defn- request-token
  "POSTs request to OAauth2 provider for authorization token."
  [{:keys [uri-config access-token-parsefn]} code]
  (println "Getting access token from OAuth2 provider" code)
  (let [access-token-uri (:access-token-uri uri-config)
        query-map        (-> (util/replace-authz-code access-token-uri code)
                             (assoc :grant_type "authorization_code"))
        token-parse-fn   (or access-token-parsefn util/extract-access-token)]
    (do
      (println "hehe" (:url access-token-uri) query-map)
      (token-parse-fn (client/post (:url access-token-uri) {:form-params query-map})))))

(defn- redirect-to-provider!
  "Redirects user to OAuth2 provider. Code should be in response."
  [{:keys [uri-config]} request]
  (let [anti-forgery-token    (util/generate-anti-forgery-token)
        session-with-af-token (assoc (:session request) (keyword anti-forgery-token) "state")]
    (-> uri-config
        (util/format-authn-uri anti-forgery-token)
        ring.util.response/redirect
        (assoc :session session-with-af-token))))

(defn workflow
  "Workflow for OAuth2"
  [config]
  (fn [request]
    (println "========>>>" (:uri request))
    (if (is-oauth2-callback? config request)
      (do
        (println "This is a callback.")
        (pprint/pprint request))
      (println ""))
    (when (is-oauth2-callback? config request)
      ;; Extracts code from request if we are getting here via OAuth2 callback.
      ;; http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.1.2
      (let [myparams (:params (ring-params/params-request request))
            nope (pprint/pprint (ring-params/params-request request))
            state (:state myparams)
            code (:code myparams)
            error (:error myparams)
            session-state        (util/extract-anti-forgery-token request)]
        (println "myparams " myparams)
        (println "state: " state " code: " code " error: " error " session-state: " session-state)
        (if (or false (and (not (nil? code))
                 (= state session-state)))
          (do (println "One")
            (when-let [access-token (request-token config code)]
              (when-let [auth-map ((:credential-fn config default-credential-fn)
                                   {:access-token access-token})]
                (vary-meta auth-map merge {::friend/workflow :oauth2
                                           ::friend/redirect-on-auth? true
                                           :type ::friend/auth}))))

          (do (println "Two")
            (let [auth-error-fn (:auth-error-fn config)]
              (if (and error auth-error-fn)
                (auth-error-fn error)
                (redirect-to-provider! config request)))))))))
