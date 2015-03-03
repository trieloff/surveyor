(ns surveyor.fluidsurveys-test
  (:require [clojure.test :refer :all]
            [surveyor.fluidsurveys :refer :all]))

(deftest opportunity-tester
  (testing "Regular Opportunity"
    (is (= (calculate-ulwick-opportunity 10 0) 20))
    (is (= (calculate-ulwick-opportunity 10 10) 10))
    (is (= (calculate-ulwick-opportunity 0 0) 0))
    (is (= (calculate-ulwick-opportunity 10 nil) nil))))

(deftest kano-tester
  (testing "Kano Scores"
    (is (= (calculate-kano-score 0 0) "questionable"))
    (is (= (calculate-kano-score 0 1) "attractive"))
    (is (= (calculate-kano-score 0 nil) nil))))


(deftest aggregate-tester
  (testing "Aggregations"
    (is (= (aggregate-scores [1 2 3]) 2))
    (is (= (aggregate-scores ["ho" "ho" "hi"]) "ho"))
    (is (= (aggregate-scores ["ho" "ho" "hi" nil nil nil]) "ho"))
    (is (= (aggregate-scores [1 2 3 nil]) 2))))

(filter-notnil [1 2 3 nil])

(aggregate-scores [1 2 3])

(run-tests)
