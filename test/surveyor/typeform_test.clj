(ns surveyor.typeform-test
  (:require [clojure.test :refer :all]
            [surveyor.typeform :refer :all]
            [surveyor.aha :refer [extract-custom]]))

(def multifeatures '({"release"
   {"resource" "https://blue-yonder.aha.io/api/v1/releases/SBX-R-5",
    "owner"
    {"id" "5993232104682160308",
     "name" "Lars Trieloff",
     "email" "lars.trieloff@blue-yonder.com",
     "created_at" "2014-03-21T13:21:28Z"},
    "url" "https://blue-yonder.aha.io/releases/SBX-R-5",
    "id" "6168374114888808457",
    "integration_fields" [],
    "release_date" "2014-08-24",
    "name" "3.0",
    "start_date" "2014-08-12",
    "created_at" "2015-07-06T12:41:56Z",
    "reference_num" "SBX-R-5"},
   "resource" "https://blue-yonder.aha.io/api/v1/features/SBX-29",
   "score" 0,
   "due_date" nil,
   "workflow_kind" {"id" "6053108729101086872", "name" "New"},
   "url" "https://blue-yonder.aha.io/features/SBX-29",
   "workflow_status"
   {"id" "6013053275678580297", "name" "Under consideration"},
   "requirements" [],
   "tags" [],
   "id" "6168375418677811509",
   "feature_links" [],
   "integration_fields" [],
   "created_by_user"
   {"id" "5993232104682160308",
    "name" "Lars Trieloff",
    "email" "lars.trieloff@blue-yonder.com",
    "created_at" "2014-03-21T13:21:28Z"},
   "position" 1,
   "name" "Heating",
   "updated_at" "2015-07-06T12:55:57Z",
   "comments_count" 0,
   "custom_fields"
   [{"key" "survey", "value" "", "type" "string"}
    {"key" "outcome",
     "value"
     "The room temperature is increased when it's cold outside",
     "type" "string"}],
   "start_date" nil,
   "created_at" "2015-07-06T12:46:59Z",
   "score_facts" [],
   "reference_num" "SBX-29",
   "assigned_to_user" nil,
   "attachments" [],
   "description"
   {"id" "6168375418682308958",
    "body" "",
    "created_at" "2015-07-06T12:46:59Z",
    "attachments" []}}
  {"release"
   {"resource" "https://blue-yonder.aha.io/api/v1/releases/SBX-R-5",
    "owner"
    {"id" "5993232104682160308",
     "name" "Lars Trieloff",
     "email" "lars.trieloff@blue-yonder.com",
     "created_at" "2014-03-21T13:21:28Z"},
    "url" "https://blue-yonder.aha.io/releases/SBX-R-5",
    "id" "6168374114888808457",
    "integration_fields" [],
    "release_date" "2014-08-24",
    "name" "3.0",
    "start_date" "2014-08-12",
    "created_at" "2015-07-06T12:41:56Z",
    "reference_num" "SBX-R-5"},
   "resource" "https://blue-yonder.aha.io/api/v1/features/SBX-28",
   "score" 0,
   "due_date" nil,
   "workflow_kind" {"id" "6053108729101086872", "name" "New"},
   "url" "https://blue-yonder.aha.io/features/SBX-28",
   "workflow_status"
   {"id" "6013053275678580297", "name" "Under consideration"},
   "requirements" [],
   "tags" [],
   "id" "6168375291745802511",
   "feature_links" [],
   "integration_fields" [],
   "created_by_user"
   {"id" "5993232104682160308",
    "name" "Lars Trieloff",
    "email" "lars.trieloff@blue-yonder.com",
    "created_at" "2014-03-21T13:21:28Z"},
   "position" 2,
   "name" "Air Conditioning",
   "updated_at" "2015-07-06T12:56:01Z",
   "comments_count" 0,
   "custom_fields"
   [{"key" "survey", "value" "", "type" "string"}
    {"key" "outcome",
     "value" "The room temperature is reduced when it's hot outside",
     "type" "string"}],
   "start_date" nil,
   "created_at" "2015-07-06T12:46:30Z",
   "score_facts" [],
   "reference_num" "SBX-28",
   "assigned_to_user" nil,
   "attachments" [],
   "description"
   {"id" "6168375296042092407",
    "body" "",
    "created_at" "2015-07-06T12:46:30Z",
    "attachments" []}}))

(def custom (map extract-custom multifeatures))

(deftest transform-test
  (testing "Creating JSON"
    (is (="" (create-survey "SBX-R-5" custom "Example Survey")))))
