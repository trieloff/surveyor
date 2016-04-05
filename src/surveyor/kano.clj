(ns surveyor.kano)

(def likert [ {"code" "like", "label"
               {"en" "I like it"}}
              {"code" "must", "label"
               {"en" "I expect it"}}
              {"code" "neutral", "label"
               {"en" "I don't care"}}
              {"code" "tolerate", "label"
               {"en" "I can tolerate it"}}
              {"code" "dislike", "label"
               {"en" "I dislike it"}}])

(def kano-matrix {
 [0 0] "questionable" [0 1] "attractive"  [0 2] "attractive"  [0 3] "attractive"  [0 4] "one-dimensional"
 [1 0] "reverse"      [1 1] "indifferent" [1 2] "indifferent" [1 3] "indifferent" [1 4] "must-be"
 [2 0] "reverse"      [2 1] "indifferent" [2 2] "indifferent" [2 3] "indifferent" [2 4] "must-be"
 [3 0] "reverse"      [3 1] "indifferent" [3 2] "indifferent" [3 3] "indifferent" [3 4] "must-be"
 [4 0] "reverse"      [4 1] "reverse"     [4 2] "reverse"     [4 3] "reverse"     [4 4] "questionable"})
