(in-ns 'surveyor.core)

(def aha-options {:timeout 2000             ; ms
              :headers {"Authorization" "Bearer 4ae035c900cfc348ac8a1c7679c858928653995424b223db14c4328d621de354"}})

(defn feature-details
  "Retrieves the detailed feature description for a given feature from a
  feature list. The passed feature object needs to have a 'resource' key."
  [feature]
  (let [{:keys [status headers body error] :as string}
        @(http/get (get feature "resource") aha-options)]
    (get (parse-string body) "feature")))

(defn has-outcome
  "Returns true if the passed feature has a specified outcome custom field"
  [feature]
  (= 1 (count (filter
               #(and (= (get % "key") "outcome") (not= (get % "value") ""))
               (get feature "custom_fields")))))

(defn get-features
  "Retrieves a lazy seq of features for a given release, starting at the
  specified page. More features are retrieved until the end of the seq"
  ([release]
   (get-features release 1))
  ([release page]
   (let [{:keys [status headers body error] :as string}
         @(http/get (str
                     "https://blue-yonder.aha.io/api/v1/releases/"
                     release
                     "/features?per_page=5&page="
                     page) aha-options)]
     (let [features
           (pmap feature-details (get (parse-string body) "features"))]
       (if (< (count features) 5)
         (map feature-details features)
         (lazy-seq (into features (get-features release (inc page)))))))))
