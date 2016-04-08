(ns surveyor.aggregates
  (:require [clojure.string :refer :all])
  (:require [clojure.math.numeric-tower :refer :all]))

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



;(1 – NPS) ^ 2    *     #P/#T                     + (0 - NPS) ^ 2 * #N/#T                         + (-1 - NPS) ^ 2 * #D/#T

(defn aggregate-nps [coll]
  "Calculate Net Promoter Score with variance and min/max bounds."
  (let [responents (count coll)
        promoters  (count (filter #(> % 8) coll))
        detractors (count (filter #(< % 7) coll))
        nps        (quot (* 100 (- promoters detractors)) responents)
        neutrals   (- responents promoters detractors)
        variance   (quot (+
                    (* (expt (- 1 nps) 2) promoters)
                    (* (expt (- nps) 2) neutrals)
                    (* (expt (- 1 nps) 2) detractors)) responents)]
    {:nps nps
     :variance  variance}))
