;; ## nothing here...
(ns clojure-exercises.core)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Q: What good is the IDENTITY function?
;; A: use identity as a predicate for HOF like filter

;; GET requests (succeeds and returns a value twice, then fails)
(def attempts ["bar" "foo" nil])

;; Filter out unsuccessful attempts using identity function
(filter identity attempts) ; => ("foo")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Vars, Symbols, Objects?

;; There's a symbol + that you can talk about by quoting it:

;; user=> '+
;; +
;; user=> (class '+)
;; clojure.lang.Symbol
;; user=> (resolve '+)
;; #'clojure.core/+

;; So it resolves to #'+, which is a Var:

;; user=> (class #'+)
;; clojure.lang.Var

;; The Var references the function object:

;; user=> (deref #'+)
;; #<core$_PLUS_ clojure.core$_PLUS_@55a7b0bf>
;; user=> @#'+
;; #<core$_PLUS_ clojure.core$_PLUS_@55a7b0bf>

;; (The @ sign is just shorthand for deref.) Of course the usual way
;; to get to the function is to not quote the symbol:

;; user=> +
;; #<core$_PLUS_ clojure.core$_PLUS_@55a7b0bf>

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CONDP
(defn tt [x]
  (condp = x
    1 x
    2 (str x)
    "nothing"))

(defn ttt [ds]
  (condp resolve ds
    nil :>> deref
    nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ARITY OVERLOADING

(defn string->integer [base str]
  (Integer/parseInt str base))

(def decimal (partial string->integer 10))
(decimal "10")
;10

(def db nil)

(defn do-it-overloaded
  ([db] (println db))
  ([db stuff] (println stuff)))

(def do-it
  (partial do-it-overloaded "new"))

(do-it "hey")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Convert to delimited string

(clojure.string/join "," ["a" "b"])
;; is better than this:
(apply str (interpose "," ["a" "b"]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; params / destructuring

(defn somefn
  [req1 req2 ;required params
   & {:keys [a b c d e] ;optional params
      :or {a 1 ; optional params with preset default values other than the nil default
               ; b takes nil if not specified on call
           c 3 ; c is 3 when not specified on call
           d 0 ; d is 0 --//--
               ; e takes nil if not specified on call
           }
      ;; takes nil if no extra params(other than the required ones) are
      ;; specified on call
      :as mapOfParamsSpecifiedOnCall
      }]
  (println req1 req2 mapOfParamsSpecifiedOnCall a b c d e))

(somefn 9 10 :b 2 :d 4)
;9 10 {:b 2, :d 4} 1 2 3 4 nil

(somefn 9 10)
;9 10 nil 1 nil 3 0 nil

(somefn 9 10 :x 123)
;9 10 {:x 123} 1 nil 3 0 nil

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; tree-seq: using it to filter an map
;; note: clojure.walk is more about transforming a map,
;;       not so much about filtering it

(def m {:content '("b" {:content ("c" {:content ("d")})} "e")})

(tree-seq associative? :content m)

(filter string? (tree-seq associative? :content m))
;=> ("b" "c" "d" "e")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; macros

(def ds1
  {:_id 1
   :substr "FL000"
   :concat1 "con"
   :concat2 "cat"
   :reduce-val "NY"
   :word "tres"})

(defmacro tm [x]
  (not-any? nil? (map resolve (filter symbol? (flatten x)))))

(tm (:word ds1))

(defmacro tt [& args]
  (first (filter
          (fn [x] (not-any? nil? (map resolve (filter symbol? (flatten x)))))
          args)))

(macroexpand '(tt (:_id nope) (:_id ds1)))

(eval (tt (:_id nope) (:word ds1)))

(boolean (resolve 'ds1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; handy if you have a list of keys you want
;; like juxt, but the keys are in a vector
(map {:a 1, :b 2, :c 3, :d 4} [:a :d]) ;=> (1 4)

;; simple way to keep an ordered list of tuples
(def tuples
  [:a "A"
   :b "C"])

(take-nth 2 tuples) ;=> (:a :b)
(take-nth 2 (rest tuples)) ;=> ("A" "C")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; function composition
;; (similar to macro, isnt' it?)

;; Example 1:
(defn fnth [n]
  (apply comp
         (cons first
               (take (dec n) (repeat rest)))))

;; creates a list of 3 rest functions
(take 3 (repeat rest))
;=> (#<core$rest clojure.core$rest@2166a8a0>
;=> #<core$rest clojure.core$rest@2166a8a0>
;=>  #<core$rest clojure.core$rest@2166a8a0>)

;; add the first fn to the list
(cons first (take 3 (repeat rest)))
;=> (#<core$first clojure.core$first@54317674>
;=>  #<core$rest clojure.core$rest@2166a8a0>
;=>  #<core$rest clojure.core$rest@2166a8a0>
;=>  #<core$rest clojure.core$rest@2166a8a0>)

;; apply: takes a fn and a sequence of things and effectively calls the fn with
;;        the list elements as its arguments. Basically comverts a sequence like
;;        a list or vector of things into arguments to send to a function.
;; comp: compose (return a fn that is the composition of the fns).
(apply comp (cons first (take 3 (repeat rest))))
;=> #<core$comp$fn__4196

;; Example 2:

;; '(a B C) is a list of SYMBOLS. name returns the string of a symbol.
(map (comp keyword #(.toLowerCase %) name) '(a B C))
;=> (:a :b :c)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; apply is similar to R's vectorize
;; In R, a function is vectorized if it can be applied to a vector as well as
;; a list of arguments

;; Clojure apply: takes a fn and a sequence of things and effectively calls the
;;        fn with the list elements as its arguments. Basically comverts a
;;        sequence like a list or vector of things into arguments to send to a
;;        function.

;; x <- 1:4; y <- 6:9; x; y;
;; x + y # add the vectors together, returns a new vector

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FOR vs DOSEQ (lazy or side-effecting?)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Whenever you see do, as in do, doseq, dosync, etc. you know you are dealing
;; with side-effects, and imperative code vs. purely functional code
;; TIP: remove all computation from side-effecting functions, leaving only
;;      side effects.

;; for: for builds a lazy seq and returns it
;; doseq: executes side-effects and returns nil
(for [x [1 2 3]] (+ x 5))
;; (6 7 8)
(for [x [1 2 3]] (println x))
;; If executed in repl, repl forces lazy seq, causing println's to happen:
;; 1
;; 2
;; 3
;; (nil nil nil)
;; Otherwise, in non-interactive environment, no println's:
;; (nil nil nil)
(doseq [x [1 2 3]] (+ x 5))
;; nil
(doseq [x [1 2 3]] (println x))
;; 1
;; 2
;; 3
;; nil

;; For returns a lazy sequence, while doseq performs side-effects
;; immediately. So, if you're doing side-effects in a for, they won't occur
;; until you try to use the values from the for (printing at the REPL counts as
;; using the values.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CONJ (conjoin) vs. CONCAT (concatenate) vs. CONS (construct) vs MERGE

;; CONJ: add element to coll (add may happen in different places)
(conj [0] 1)
;=> [1 0]
(conj '(0) 1)
;=> (0 1) ; 0 added at the beginning!

;; CONCAT: 'merge' colls, returning a lazy seq
(concat [0] [1])
;=> (0 1)

;; CONS: add element to beginning of coll, returning a seq
(cons 0 [1])
;=> (0 1)

;; MERGE: (merge & maps) ; note: maps only
(merge {:a 1 :b 2} {:b 3 :c 4})
;=> {:a 1 :b 3 :c 4} ; note: last b is used (previous is discarded)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WORKING ON A SEQUENCE OF MAPS

(merge-with +
            {:a 1  :b 2}
            {:a 9  :b 98 :c 0})
;;=> {:c 0, :a 10, :b 100}

(select-keys {:a 1 :b 2 :c 3} [:a :c])
;;=> {:c 3, :a 1}

(def v [{:char "a"} {:char "b"}])
(vec (map :char v))
;;=> ["a" "b"]
;; much better than this:
(reduce (fn [acc iter] (conj acc (:char iter))) [] v)
;;=> ["a" "b"]

(def vm [{:a 1 :b 2} {:a 3 :b 4}])

;; (reduce-kv (fn [m k v] (assoc m k (inc v))) {} vm)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Transducers

(def xf (comp (filter odd?) (map inc)))
(transduce xf + (range 5))
;; => 6

;; approach 1: nested calls
(reduce + (filter odd? (map #(+ 2 %) (range 0 10))))
;=> 35  result of (+ 3 5 7 9 11)

;; approach 2: function composition
(def xform (comp (partial filter odd?) (partial map #(+ 2 %))))
(reduce + (xform (range 0 10)))
;=> 35

;; approach 3: transducers
(def xform (comp (map #(+ 2 %)) (filter odd?)))
;; Note: filter & map's third arg (the collection) are missing -
;; because map & filter now have an arity that produces a transducer.
;; For example, (filter odd?) is a transducer. Note that transducers compose
;; through ordinary function composition like above.
(transduce xform + (range 0 10))
;=> 35

;; transducers don't get called directly - they get passed to another function.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reduce + [1 1 1 1])
;=> 4

;; reductions: shows every iteration of a reduce
(reductions + [1 1 1 1])
;=> (1 2 3 4)

