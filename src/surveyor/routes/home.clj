(ns surveyor.routes.home
  (:require [compojure.core :refer :all]
            [hiccup.element :refer :all]
            [surveyor.views.layout :as layout]))

(defn home []
  (layout/common [:h1 "Hello World!"]
                 (link-to "/aha.callback" "Login with aha.io")))

(defn unauthorized []
  (layout/common [:h1 "Get off my lawn"]))

(defroutes home-routes
  (GET "/" [] (home)))
