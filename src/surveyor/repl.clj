(ns surveyor.repl
  (:use surveyor.handler
        ring.server.standalone
        surveyor.config
        [ring.middleware file-info file]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
    ; Makes static assets in $PROJECT_DIR/resources/public/ available.
    (wrap-file "resources")
    ; Content-Type, Content-Length, and Last Modified headers for files in body
    (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (println "aha.host" (config "aha.host"))
  (println "aha.clientdomain" (config "aha.clientdomain"))
  (println "aha.clientid" (clojure.string/replace (config "aha.clientid") #"(.*)(....)" "•••$2"))
  (println "aha.clientsecret" (clojure.string/replace (config "aha.clientsecret") #"(.*)(....)" "•••$2"))
  (println "fluidsurveys.auth" (clojure.string/replace (config "fluidsurveys.auth") #"(.*)(....)" "•••$2"))
  (let [port (if port (Integer/parseInt (clojure.string/join "" port)) 3000)]
    (reset! server
            (serve (get-handler)
                   {:port port
                    :init init
                    :auto-reload? true
                    :destroy destroy
                    :join true}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))

(defn -main [& args]
  (println "Starting REPL")
  (start-server args)
)
