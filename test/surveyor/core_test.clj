(ns surveyor.core-test
  (:require [clojure.test :refer :all]
            [surveyor.core :refer :all]
            [surveyor.fluidsurveys :refer :all]
            [clojure.data :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest label-generator
  (testing "Generation of scale labels"
    (is (= (create-labels 0 10) [
                       {"label"
                        {"en" "0"}}
                       {"label"
                        {"en" "1"}}
                       {"label"
                        {"en" "2"}}
                       {"label"
                        {"en" "3"}}
                       {"label"
                        {"en" "4"}}
                       {"label"
                        {"en" "5"}}
                       {"label"
                        {"en" "6"}}
                       {"label"
                        {"en" "7"}}
                       {"label"
                        {"en" "8"}}
                       {"label"
                        {"en" "9"}}
                       {"label"
                        {"en" "10"}}]))))

(run-tests)
