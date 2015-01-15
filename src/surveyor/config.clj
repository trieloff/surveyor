(use 'clojure.java.io)

(defn load-props
  ([file-name] (load-props file-name {}))
  ([file-name config]
  (if (.exists (as-file file-name)) (with-open [^java.io.Reader reader (clojure.java.io/reader file-name)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into config props)))
    {}))
)

(defn override-props [config]
  (reduce merge config (map (fn [[key value]] {key (or (System/getenv(string/upper-case (string/replace key #"\." "_"))) value)}) (vec config) ))
)

(def load-config (memoize #(override-props (load-props "surveyor.properties" (load-props "surveyor-default.properties")))))

(defn config [key]
  (get (load-config) key "")
)
