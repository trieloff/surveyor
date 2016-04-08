(ns surveyor.aggregates
  (:require [clojure.string :refer :all]))

(defn mean [coll]
   (/ (reduce + coll) (count coll)))

(defn median [coll]
  (nth (sort coll)(int (/ (count coll) 2))))

(defn mode [coll]
  (first (last (sort-by second (frequencies coll)))))

(defn aggregate-text [coll]
  "Return term frequencies for the words in the collection of strings"
  (frequencies (split (clojure.string/join " " coll) #"\s")))

(aggregate-text
  '("Hallo Welt" "Hello World" "Bonjour Monde" "Hallo World"))


