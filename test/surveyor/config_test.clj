(ns surveyor.config-test
  (:require [clojure.test :refer :all]
            [surveyor.config :refer :all]
            [clojure.data :refer :all]))

(deftest test-config
  (testing "loading an empty configuation keeps default intact"
    (is (not= (load-props "nonexistant.properties" (load-props "surveyor-default.properties")) {})))
  (testing "loading default configurations from file works"
    (is (= (get (load-props "surveyor-default.properties") "aha.host") "test")))
  (testing "loading default configurations"
    (is (= (config "aha.host") "test"))))

(deftest test-envs
  (testing "Environment variables get loaded"
    (is (= (config "yxlipyx") "")))
  (testing "Environment variables get loaded"
    (is (not= (config "path") "")))
  (testing "Environment variables get loaded"
    (is (not= (config "path") "/tmp")))
  (testing "Environment variables get loaded"
    (is (= (System/getenv "FOO_BAR") "baz")))
  (testing "Environment variables get loaded"
    (is (not= (config "foo.bar") "baz")))
  )

(load-props "surveyor-default.properties")


(config "path")



(run-tests)
