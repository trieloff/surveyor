(defproject surveyor "0.1.0-SNAPSHOT"
  :description "Create surveys from feature sets"
  :url "https://github.com/trieloff/suveyor"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.16"]
                 [clj-time "0.6.0"]
                 [cheshire "5.3.1"]]
  :main surveyor.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
