(ns surveyor.routes.home
  (:require [compojure.core :refer :all]
            [hiccup.element :refer :all]
            [cemerick.friend :as friend]
            [surveyor.aha :as aha]
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
  (layout/common [:h1 "Select Release"]
                 (link-to "/aha.info" "back")
                 [:ul
                  (for [x (aha/get-releases product token)]
                    [:li (link-to (str "/aha/" (:reference_prefix x)) (:name x)) " – " (-> x :workflow_status :name)])]))

(defn home []
  (println "home")
  (layout/common [:h1 "Hello World!"]
                 [:ul
                  [:li (link-to "/aha.info" "Login with aha.io")]
                  [:li (link-to "/aha.nope" "Nope with aha.io")]
                  [:li (link-to "/status" "Get status")]]))

(defn unauthorized
  [request]
  {:status 403
   :body (layout/common "Piss off, you do not have access to this resource."
                        (link-to "/" "Back."))})

(defroutes home-routes
  (GET "/" [] (home))
  (GET "/aha.nope" request
         (render-aha-info request))
  (GET "/aha/:product" [product :as request]
       (friend/authorize #{:surveyor.handler/user} (render-aha-product product (-> request :session :cemerick.friend/identity :current :access-token) request)))
  (GET "/aha.info" request
       (friend/authorize #{:surveyor.handler/user} (render-aha-info request)))
  (GET "/status" request
       (render-status-page request)))
