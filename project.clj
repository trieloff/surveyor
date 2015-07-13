(defproject surveyor "0.1.0-SNAPSHOT"
  :description "Create surveys from feature sets"
  :url "https://github.com/trieloff/suveyor"
  :min-lein-version "2.5.0"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.16"]
                 [clj-time "0.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [compojure "1.3.0"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 [friend-oauth2 "0.1.3"]
                 [sudharsh/clj-oauth2 "0.5.3"]]
  :main surveyor.core
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler surveyor.handler/app
         :init surveyor.handler/init
         :destroy surveyor.handler/destroy}
  :uberjar-name "surveyor-standalone.jar"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
