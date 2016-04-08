(ns surveyor.util)


(defn map-a-map [f m]
  "Equivalent to map on the values of a map"
  (into {} (for [[k v] m] [k (f v)])))

(defn grouped-map-by [f m]
  "Similar to group-by, but cleans up the resulting map by removing the grouping key f"
  (map-a-map
    (fn [maps] (map #(dissoc % f) maps))
    (group-by f m)))
