(ns surveyor.util)


(defn map-a-map [f m]
  (into {} (for [[k v] m] [k (f v)])))

