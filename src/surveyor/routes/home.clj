(ns surveyor.routes.home
  (:require [compojure.core :refer :all]
            [hiccup.element :refer :all]
            [hiccup.form :refer :all]
            [clojure.string :as string]
            [cemerick.friend :as friend]
            [surveyor.aha :as aha]
            [surveyor.core :as core]
            [surveyor.views.layout :as layout]))


(defn render-status-page [request]
  (let [count (:count (:session request) 0)
        session (assoc (:session request) :count (inc count))]
    (-> (ring.util.response/response
           (str "<p>We've hit the session page " (:count session)
                " times.</p><p>The current session: " session "</p><p><a href='/'>back</a>"))
         (assoc :session session))))

(defn render-aha-info [request]
  (println "aha")
  (let [token (-> request :session :cemerick.friend/identity :current :access-token)]
    (layout/common [:h1 "Select Product"]
                   [:ul
                    (for [x (aha/get-products token)]
                      [:li (link-to (str "/aha/" (:reference_prefix x)) (:name x))])])))

(defn render-aha-product [product token request]
  (println token)
  (layout/common [:h1 "Select Releases"]
                 (link-to "/aha.info" "back")
                 (form-to ["POST" (str "/aha/" product)]
                          [:ul
                           (for [x (aha/get-releases product token)]
                             [:li x
                              (check-box "releases" false (:reference_num x))
                              (link-to (str "/aha/" product "/" (:reference_num x)) (label "releases" (:name x))) " – " (-> x :workflow_status :name)])]
                          [:div
                           (check-box "filters" false "score")
                           (label "filters" "Override scores")]
                          [:div
                           (check-box "filters" false "survey")
                           (label "filters" "Override survey links")]
                          [:div
                           (check-box "filters" false "deleted")
                           (label "filters" "Include deleted features")]
                          (submit-button "Create multi-release survey"))))

(defn render-aha-release [product release token request]
  (layout/common [:h1 "This is a release"]
                 (link-to (str "/aha/" product) "back")
                 (form-to ["POST" (str "/aha/" product "/" release)]
                          (hidden-field "action" "create")
                          [:div
                            (check-box "filters" false "score")
                            (label "filters" "Override scores")]
                          [:div
                            (check-box "filters" false "survey")
                            (label "filters" "Override survey links")]
                          [:div
                            (check-box "filters" false "deleted")
                            (label "filters" "Include deleted features")]
                          (submit-button "Create Survey"))
                 (form-to ["POST" (str "/aha/" product "/" release)]
                          (hidden-field "action" "retrieve")
                          (submit-button "Retrieve Results"))))

(defn update-aha-release [product release token action filters request]
  (layout/common [:h1 "Updating a release"]
                 ;;                  [:code (str request)]
                 [:p action]
;;                  [:code (str filters)]
                 (if (= action "create")
                   (let [survey (core/make-survey-for-release release token filters)]
                     [:p "Survey has been created: "
                      (link-to survey survey)])
                   (let [result (core/save-results-for-release (core/merge-results-for-release release token) token)]
                     [:p "Stuff has been updated"]
                     [:code result]
                     ))
                 (link-to (str "/aha/" product "/" release) "done.")))

(defn update-aha-releases [product releases token filters request]
  (layout/common [:h1 "Creating multi-release survey for " (string/join ", " releases)]
                 [:code (str filters)]
                 (link-to (str "/aha/" product) "done.")))

(defn home []
  (println "home")
  (layout/common [:h1 "Hello World!"]
                 [:ul
                  [:li (link-to "/aha.info" "Login with aha.io")]
                  [:li (link-to "/aha.nope" "Logout of aha.io")]
                  [:li (link-to "/status" "Get status")]]))

(defn unauthorized
  [request]
  {:status 403
   :body (layout/common "I'm very sorry, you do not have access to this resource."
                        (link-to "/" "Back."))})

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/aha.nope" request
         (render-aha-info request))
  (GET "/debug" [:as request]
       (friend/authorize #{:surveyor.handler/user} (str request)))
  (GET "/aha/:product/:release" [product release :as request]
       (friend/authorize #{:surveyor.handler/user} (render-aha-release product release (-> request :session :cemerick.friend/identity :current :access-token) request)))
  (POST "/aha/:product/:release" [product release :as request]
        (friend/authorize #{:surveyor.handler/user} (update-aha-release
                                                     product
                                                     release
                                                     (-> request :session :cemerick.friend/identity :current :access-token)
                                                     (get (:params request) "action")
                                                     (vec (map #(keyword "surveyor.aha" (str %)) (flatten (vector (get (:params request) "filters")))))
                                                     request)))
  (GET "/aha/:product" [product :as request]
       (friend/authorize #{:surveyor.handler/user} (render-aha-product product (-> request :session :cemerick.friend/identity :current :access-token) request)))
  (POST "/aha/:product" [product release :as request]
        (friend/authorize #{:surveyor.handler/user} (update-aha-releases
                                                     product
                                                     (filter (comp not nil?) (flatten (vector (get (:params request) "releases"))))
                                                     (-> request :session :cemerick.friend/identity :current :access-token)
                                                     (vec (map #(keyword "surveyor.aha" (str %)) (flatten (vector (get (:params request) "filters")))))
                                                     request)))
  (GET "/aha.info" request
       (friend/authorize #{:surveyor.handler/user} (render-aha-info request)))
  (GET "/status" request
       (render-status-page request)))
