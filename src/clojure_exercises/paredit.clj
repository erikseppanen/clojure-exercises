(ns clojure-exercises.paredit)

;; ; Keeps parenthesis balanced.
;; ; Some structural editing examples (emacs + paredit):

;; ; C-M-SPC to select sexp [emacs mark-sexp]
;; ; then kill/yank/quote

;; (defn next-two-throws [frames]
;;   (let [frame (first frames)
;;         digits (map str frame)]
;;     (cond
;;       (= frame "X") (+ 10 (next-one-throw (rest frames)))
;;       (= frame "XX") 20
;;       (= (second digits) "/") 10
;;       ;; M-; (paredit)
;;       :else (reduce + (map read-string digits)))))

;; C-M-SPC multiple times (emacs)

;; C-k [paredit kill] & C-M-k [emacs kill sexp]
;;    (kill just this) "but not this"

;; navigation: combination of [emacs subword] commands (M-f & M-b)
;; & [paredit forward/backward] (C-M-f & C-M-b).
;; [Paredit forward-up & down]: I don't use that much.

;; [Paredit splice] commands: M-up, M-down & M-s

;; (defn fizzbuzz
;;   [arg]
;;   (cond
;;     (= 0 (mod arg 3))  "Fizz"
;;     (= 0 (mod arg 5))  "Buzz"
;;     (= 0 (mod arg 15)) "FizzBuzz"
;;     :else (str arg)))

;; [Paredit reindent] M-q to fix indentation
;; can also reduce long comments to 80 columns (if point is in comment)

;; M-^ (emacs delete indentation) to remove indentation at end of sexp
;; Craig: can just type end-parens inside, and it puts them at the end
;; automatically

;; (defn test
;;   "Pug fap Vice organic stumptown. Single-origin coffee hashtag
;;   mixtape McSweeney's fanny pack paleo Marfa PBR&B, leggings
;;   Shoreditch High Life."
;;   [a b]
;;   (println a))

;; ;; paredit's slurp/barf forward/backward C-) C-}
;; str ('this 'and 'this)

