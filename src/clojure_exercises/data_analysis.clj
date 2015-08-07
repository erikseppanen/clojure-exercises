(ns clojure-exercises.data-analysis)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Reading text files

(require '[clojure.java.io :as io])

(defn parse-resource-file [res parse]
  (with-open [rdr (io/reader (io/resource res))]
    (doseq                              ; do for each line
        [line (line-seq rdr)]           ; return lines as string seq
      (parse line))))                   ; apply fn to each line

(defn copy-file [source-path dest-path]
  (io/copy (io/file source-path) (io/file dest-path)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Identifying and removing duplicate data

(require '[clj-diff.core :as diff])

(defn fuzzy=
  "Returns a fuzzy match.

  If up to 2 character changes are made, it is considered a match
  (this is for short strings).  On the other hand, if the strings are
  up to 10% different, they are considered a match (this is for long
  strings)

  clj-diff's edit-distance returns a similarity metric for 2
  strings (lower is more alike). Caveat: this doesn't work well for
  small words, like 'ace' vs 'age', but it can easily be improved.
  Levenshtein-distance can also be used (measures single-character
  replace operations)."
  [a b]
  (let [dist (diff/edit-distance a b)]
    ;; set max diff to 2 (allow up to 2 changes [del/ins])
    (or (<= dist 2)
        (<= (/ dist (min (count a) (count b)))
            ;; set max percent diff to 0.1
            0.1))))

(defn records-match
  "Determines if two collections are a match."
  [key-fn a b]
  ;; If not sequential, convert to vector
  ;;  sequential? asks the collection if it implements the Seq interface,
  ;;  which is a promise that the collection can be iterated over
  ;;  in a defined order
  (let [kfns (if (sequential? key-fn) key-fn [key-fn])
        rfn (fn [prev next-fn]
              (and prev (fuzzy= (next-fn a)
                                (next-fn b))))]
    ;; start by assuming it is a match (true), then iterate over kfns's
    ;; keywords, evaluating each, until the 1st false. If all T, return T.
    (reduce rfn true kfns)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Synonym Maps

(def state-synonyms
  {"ALABAMA" "AL",
   "ALASKA" "AK",
   "ARIZONA" "AZ",
   "WISCONSIN" "WI",
   "WYOMING" "WY"})

(defn normalize-state [state]
  (let [uc-state (clojure.string/upper-case state)]
    (state-synonyms uc-state uc-state)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Normalize numbers

(defn normalize-number
  [n]
  (let 
      ;; Split on every period & comma
      [v (clojure.string/split n #"[,.]")
       ;; Take the last part as the decimal part (post)
       ;; and everything before that as the integer part (pre)
       [pre post] (split-at (dec (count v)) v)]
    ;; Put the number back to together & cast as Double
    (Double/parseDouble (apply str (concat pre [\.] post)))))

; (normalize-number "1,000.00") ;=> 1000.0
; (normalize-number "1.000,00") ;=> 1000.0

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Normalizing dates and times

(require '[clj-time.format :as format])

(def ^:dynamic *default-formats*
         [:date
          :date-hour-minute
          :date-hour-minute-second
          :date-hour-minute-second-ms
          :date-time
          :date-time-no-ms
          :rfc822
          "YYYY-MM-dd HH:mm"
          "YYYY-MM-dd HH:mm:ss"
          "dd/MM/YYYY"
          "YYYY/MM/dd"
          "d MMM YYYY"])

(defprotocol ToFormatter
         (->formatter [fmt]))

(extend-protocol ToFormatter
         java.lang.String
         (->formatter [fmt]
           (clj-time.format/formatters fmt))
         clojure.lang.Keyword
         (->formatter [fmt]
           (clj-time.format/formatters fmt)))

(defn parse-or-nil
         [fmt date-str]
         (try(clj-time.format/parse (->formatter fmt) date-str)
             (catch Exception ex nil)))

(defn normalize-datetime
         [date-str]
         (first (remove nil?
                        (map #(parse-or-nil % date-str)
                             *default-formats*))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Incanter

; (use '(incanter core stats io datasets))
(require '[incanter.core :as icore]
         '[incanter.stats :as stats]
         '[incanter.io :as iio]
         '[incanter.datasets :as ds])

;; Save iris dataset to disk, then use that (so we can work disconnected)
;; (def iris (get-dataset :iris))
;; (incanter.core/save iris "/tmp/iris.csv")
(def iris (iio/read-dataset (clojure.java.io/resource "iris.csv") :header true))

;; IRIS: the Fisher's or Anderson's Iris data set gives the
;; measurements in centimeters of the variables sepal length and width
;; and petal length and width, respectively, for 50 flowers from each
;; of 3 species of iris.

;; group-by: groups rows (keeping row data intact-not just aggregate values)
(def by-species (icore/$group-by :Species iris))
(def grp (icore/$ [0 1 2 3] :all
                  (by-species {:Species "setosa"})))

;; joins:
(def lookup (icore/dataset [:species :species-key]
                           [["setosa" :setosa]
                            ["versicolor" :versicolor]
                            ["virginica" :virginica]]))

;; join lookup & iris datasets
(icore/$join [:species :Species] lookup iris)

(defn col-seq [ds] (map name (icore/col-names ds)))

(defn data-types [ds] (map (fn [x] (str (type x))) (icore/$ 0 :all ds)))

(defn statible? [x] (= x "class java.lang.Long"))

(defn mean2 [x]
  (let [stats (map statible? (data-types x))]
    (map (fn [x] ()) x)))

(defn summary2 [ds]
  (let [cols (col-seq ds)
        types (data-types ds)
        rbind (vector cols types)
        transpose (apply mapv vector rbind)]
    (icore/to-dataset transpose)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Data-analysis/Transformations
;; (An example that might not make total sense
;; but illustrates the approach)

;; (def us-arrests (ds/get-dataset :us-arrests))
;; (icore/save us-arrests "/tmp/us-arrests.csv")
(def us-arrests
  (iio/read-dataset (clojure.java.io/resource "us-arrests.csv")
                    :header true))

;; US-ARRESTS: This data set contains statistics, in arrests per
;; 100,000 residents for assault, murder, and rape in each of the 50
;; US states in 1973. Also given is the percent of the population
;; living in urban areas.

;;(def unit-category (db/get-maps staging "unit-category"))
;; (def unit-category {})

;; this is a calculation
(defn high-amount?
  "Is there a large amount? Y/N"
  [n]
  (cond (> n 10) "Y"
        :else "N"))

;; this adds derived columns (calculated from other other columns)
(defn transf [data]
  (->> data
       (icore/add-derived-column :HighMurder [:Murder] high-amount?)
       (icore/add-derived-column :First4 [:State] #(subs % 0 4))
       ))

;; these are the only columns we plan to keep when we're done
(def useful-columns
  [:State :First4 :Murder :HighMurder :UrbanPop])

;; the 'pipeline'
(def pipeline
  (->  us-arrests
       (icore/to-dataset)               ; Convert to dataset
       ;; Optional: if it is a huge dataset, include just enough for analysis
       ;; (stats/sample :size 1000 :replacement false)
       (transf)                         ; Add derived columns
       (icore/sel :cols useful-columns) ; Slice off useless columns
       ))

;; various filtered/ordered results
(def murder-where (icore/$where {:HighMurder "Y"} pipeline))
(def murder-ordered (icore/$order :Murder :desc pipeline))
(def murder-where-ordered
  (->> pipeline
       (icore/$where {:First4 {:$in #{"New " "Sout"}}})
       (icore/$order :Murder :desc)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Data-analysis/Summary Statistics

;; (def census (parse-resource-file "census.csv" ))
;; (def census (iio/read-dataset (clojure.java.io/resource "census.csv") :header true))

;; (icore/$rollup :mean :POP100 :STATE census)
