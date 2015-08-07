(ns clojure-exercises.schema)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Herbert: a convenient schema language for defining edn data
;; structures that can be used for documentation and validation. The
;; schema patterns are represented as edn values.

(require '[miner.herbert :as h])

(h/conforms? 'int 10)
;=> true

(h/conforms? '{kw int} '{:a 3 :b 5})
;=> true

(h/conforms? '(set "Mr." "Mrs." "Ms.") '{})
;=> true

(h/conforms? '(grammar int) 10) ;=> true
; a very simple "grammar" with no rules, equivalent to the start pattern

(h/conforms? '{:a int :b sym :c? [str*]} '{:a 1 :b foo}) ;=> true
; :c is optional so it's OK if it's not there at all.

; but if :c is there, it will be a vector of strings ;=> true
(h/conforms? '{:a int :b sym :c? [str*]} '{:a 1 :b foo :c ["foo" "bar" "baz"]})

(h/conforms? '{:a (:= A int) :b sym :c? [A+]} '{:a 1 :b foo :c [1 1 1]}) ;=> true
; _A_ is bound to the int associated with :a, and then used again to
; define the values in the seq associated with :c.

; :a's value must be larger than :b's value
(def x '{:a (:= N int) :b (& (:= F float) (> N F))})
(h/conforms? x '{:a 4 :b 3.14}) ;=> true
(h/conforms? x '{:a 2 :b 3.14}) ;=> false

(h/conforms?
  '(& {:a (:= A int) :b (:= B sym) :c (:= C [B+])} (when (= (count C) A))) 
  '{:a 2 :b foo :c [foo foo]}) ;=> true
; The & operator just means the following elements are found inline,
; not in a collection.  In this case, we use it to associate the
; when-test with the single map constraint.  The assertion says that
; number of elements in the :c value must be equal to the value
; associated with :a.  Notice that all the elements in the :c seq
; must be equal to the symbol associated with :b.

((h/conform '[(:= A int) (:= B int) (:= C int+ A B)]) [3 7 4 5 6])
; Inside a seq, the first two ints establish the low and high range of the rest
; of the int values.
;=> {C [4 5 6], B 7, A 3}

(def my-checker (h/conform '[(:= MAX int) (:= XS int+ MAX)]))
(my-checker [7 3 5 6 4])
;=> {XS [3 5 6 4], MAX 7}

;; Conclusions: 1. Herbert does not indicate where the schema is
;; failing, just true or false for everything.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # Prismatic Schema: for declarative data description and
;; validation.
;;
;; A Schema is a Clojure(Script) data structure describing a data
;; shape, which can be used to document and validate functions and
;; data.
;;
;; One of the difficulties with bringing Clojure into a team is the
;; overhead of understanding the kind of data (e.g., list of strings,
;; nested map from long to string to double) that a function expects
;; and returns. While a full-blown type system is one solution to this
;; problem, we present a lighter weight solution: schemas.
;;
;; As of version 0.2.0, Schema also supports schema-driven data
;; transformations, with coercion being the main application fleshed
;; out thus far.

(require '[schema.core :as s])

(def schema1
  {:a s/Str
   :b s/Int
   :c s/Keyword
   :d s/Num
   :e [s/Str]})

;; (s/validate schema1 {:a "abc" :b 3 :c :name :d 1.02 :e ["a" "b" "c"]})
;; => {:e ["a" "b" "c"], :c :name, :b 3, :d 1.02, :a "abc"}

;; (s/validate schema1 {:a "abc" :b 3 :c "name" :d 1.02 :e ["a" "b" "c"]})
;; => RuntimeException:
;; 1. Unhandled clojure.lang.ExceptionInfo
;;    Value does not match schema: {:c (not (keyword? "name"))}
;;    {:error {:c (not (keyword? "name"))},
;;     :value {:e ["a" "b" "c"], :c "name", :b 3, :d 1.02, :a "abc"},
;;     :schema
;;     {:a java.lang.String,
;;      :b
;;      {:p? #<core$integer_QMARK_ clojure.core$integer_QMARK_@3a8d91fc>,
;;       :pred-name integer?},
;;      :c
;;      {:p? #<core$keyword_QMARK_ clojure.core$keyword_QMARK_@6b98dc5>,
;;       :pred-name keyword?},
;;      :d java.lang.Number,
;;      :e [java.lang.String]},
;;     :type :schema.core/error}

;; Allow schemas as type hints on fields, arguments and return values.
(s/defrecord ThingCount
    [a :- s/Str
     b :- s/Int])

(s/defn create-thing-count :- ThingCount ; fn returns ThingCount
  [input :- s/Str]                       ; fn accepts a single string
  (ThingCount. input (count input)))     ; type-hint the return value

;; (create-thing-count "abc")
;; => #exercises.data_analysis.ThingCount{:a "abc", :b 3}

