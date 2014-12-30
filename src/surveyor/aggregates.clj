(in-ns 'surveyor.core)

(defn mean [coll]
   (/ (reduce + coll) (count coll)))

(defn median [coll]
  (nth (sort coll)(int (/ (count coll) 2))))

(defn mode [coll]
  (first (last (sort-by second (frequencies coll)))))
