(ns surveyor.aha
  (:require [cheshire.core :refer :all])
  (:require [org.httpkit.client :as http])
  (:require [clojure.set :as set])
  (:require [surveyor.config :refer :all]))

(def aha-options {:timeout 2000             ; ms

              :headers {"Authorization" (str "Bearer " (config "aha.auth")) "Content-Type" "application/json"}})


(def score-names {"ulwick-importance"   "Importance"
                  "ulwick-opportunity"  "Opportunity"
                  "ulwick-satisfaction" "Satisfaction"})

(def tag-names {"one-dimensional" "linear"
                "must-be" "must-be"
                "attractive" "exciter"})

(defn token-options [token]
  (assoc-in aha-options [:headers "Authorization"] (str "Bearer " token))
)

(defn feature-details
  "Retrieves the detailed feature description for a given feature from a
  feature list. The passed feature object needs to have a 'resource' key."
  [feature token]
  (let [{:keys [status headers body error] :as string}
        @(http/get (get feature "resource") (token-options token))]
    (get (parse-string body) "feature")))

(defn has-custom?
  "Returns true if the passed feature has a specified custom field"
  [feature field]
  (= 1 (count (filter
               #(and (= (get % "key") "outcome") (not= (get % "value") ""))
               (get feature "custom_fields")))))

(defn has-outcome?
  [feature]
  (has-custom? feature "outcome")
)

(defn has-survey?
  [feature]
  (has-custom? feature "survey")
)

(defn extract-custom
  "Extracts the outcome custom field from a feature"
  [feature]
  (hash-map
   "reference_num" (get feature "reference_num")
   "resource" (get feature "resource")
   "url" (get feature "url")
   "name" (get feature "name")
   "outcome" (get (first (filter
                          #(= (get % "key") "outcome")
                          (get feature "custom_fields"))) "value")
   "survey" (get (first (filter
                          #(= (get % "key") "survey")
                          (get feature "custom_fields"))) "value")
   ))


(defn update-survey-url
  [feature survey index token]
 (let [{:keys [status headers body error] :as string}
       @(http/put (str "https://blue-yonder.aha.io/api/v1/features/" feature) (assoc (token-options token) :body (generate-string {"feature" {"custom_fields" {"survey" (str survey "#" index)}}})))]
   status)

)


(defn get-tags
  [feature token]
  (let [details (feature-details {"resource" (str "https://blue-yonder.aha.io/api/v1/features/" feature)} token)]
    (set (get details "tags"))))

(defn merge-tags [oldtags newtags]
  (set/union newtags (set/difference oldtags (set (vals tag-names)))))

(defn update-tags
  [feature tags token]
  (let [taglist (set (filter #(not (nil? %)) (map (fn [[key value]] (if (= "kano-score" key) (tag-names value))) tags)))]
    (if (seq taglist)
      (let [alltags (merge-tags (get-tags feature token) taglist)
            {:keys [status headers body error] :as string}
       @(http/put (str "https://blue-yonder.aha.io/api/v1/features/" feature) (assoc (token-options token) :body (generate-string {"feature" {"tags" (clojure.string/join "," alltags)}})))]
   taglist))))

(defn update-score
  [feature scores token]
  (let [{:keys [status headers body error] :as string}
        @(http/put
          (str "https://blue-yonder.aha.io/api/v1/features/" feature)
          (assoc (token-options token) :body (generate-string {"feature" {"score_facts"
                                                                (filter #(not (nil? %)) (map (fn [[key value]]
                                                                                               (if (score-names key)
                                                                                                 {"name" (score-names key) "value" value}))(vec scores)))}})))]
    body)
  )

(defn update-survey-urls
  [features survey token]
 (map-indexed (fn [index item] (update-survey-url (get item "reference_num") survey index token)) features)
)

(defn get-features
  "Retrieves a lazy seq of features for a given release, starting at the
  specified page. More features are retrieved until the end of the seq"
  ([release token]
   (get-features release 1 token))
  ([release page token]
   (let [{:keys [status headers body error] :as string}
         @(http/get (str
                     "https://blue-yonder.aha.io/api/v1/releases/"
                     release
                     "/features?per_page=5&page="
                     page) (token-options token))]
     (let [features
           (pmap #(feature-details % token) (get (parse-string body) "features"))]
       (if (< (count features) 5)
         (map #(feature-details % token) features)
         (lazy-seq (into features (get-features release (inc page) token))))))))

(defn get-products [token]
  (let [{:keys [status headers body error] :as string}
         @(http/get "https://blue-yonder.aha.io/api/v1/products"
                    (token-options token))]
    (filter #(not (:product_line %)) (:products (parse-string body true)))))

(defn get-product-detail [product token]
  (let [{:keys [status headers body error] :as string}
         @(http/get (str "https://blue-yonder.aha.io/api/v1/products/" product)
                    (token-options token))]
    (:product (parse-string body true))))

(defn get-release-details [release token]
  (let [{:keys [status headers body error] :as string}
         @(http/get (str "https://blue-yonder.aha.io/api/v1/releases/" release)
                    (token-options token))]
    (identity (:release (parse-string body true)))))

(defn get-releases [product token]
  (let [{:keys [status headers body error] :as string}
         @(http/get (str "https://blue-yonder.aha.io/api/v1/products/" product "/releases")
                    (token-options token))]
    (filter #(= "Under consideration" (-> % :workflow_status :name)) (map
     #(get-release-details (get % :reference_num) token)
     (:releases (parse-string body true))))))
