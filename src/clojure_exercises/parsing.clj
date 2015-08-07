(ns clojure-exercises.parsing)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; # A phone number InstaParser example (uses a spec file)

(require '[instaparse.core :as insta])

(def phone-uri-parser
  "A specification for valid phone number."
  (insta/parser
   (clojure.java.io/resource "phone_uri.txt")
   :input-format :abnf))

(def parsed-phone-number (phone-uri-parser "tel:+1-201-555-0123"))

;; # A simple mixed-delimited InstaParser example

(def car-parser
  "A specification for parsing car files. Both fixed-length and
  tab delimiters.

  Usage: (car-parser \"Toyota	Camry	41999\")"
  (insta/parser
   "<car> = make model cylinders year
     make = word <tab>
     model = word <tab>
     cylinders = #'[0-9]'
     year = #'[0-9]+'
     <word> = #'[^ \t]+'
     <tab> = #'[\t]'"
   ;; Augmented Backus-Naur Form
   :input-format :abnf))

; (parse-resource-file "cars.mixed.txt" (juxt car-parser println))

; TODO: add failing test (a row fails to be parsed)...
