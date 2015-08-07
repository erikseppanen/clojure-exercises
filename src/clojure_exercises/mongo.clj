;;;; A spike to see how to persist a clojure function in mongo and
;;;; then retrieve and evaluate it.

(ns clojure-exercises.mongo
  (:require [monger.core :as m]
            [monger.command :as cmd]
            [monger.collection :as col]
            [monger.conversion :as conv :refer [from-db-object]]
            [monger.result :as res]
            [monger.query :as q]
            [monger.operators :refer :all]
            [clojure.edn :as edn]
            )
  (:import [org.bson.types ObjectId]
           [com.mongodb MongoClient DB WriteConcern MongoClientOptions
            WriteResult])
)

;; Uncomment lines below as you go...

;; 0. Make sure mongo is running already with >mongod (mongo daemon)

;; 1. Connect to mongo

;; (def conn (m/connect (m/server-address "localhost" 27017)
;;                      (m/mongo-options {:connections-per-host 100})))

;; 2. Connect to db, authenticating with specific user
;; note: this user should have already been created like this...
;;   use mongo-spike
;;   db.addUser("test", "test")

;; (m/authenticate (m/get-db conn "mongo-spike") "test" (.toCharArray "test"))

;; 3. JOURNAL_SAFE: Exceptions are raised for network issues, and server
;; errors; the write operation waits for the server to group commit to
;; the journal file on disk.

;; (m/set-default-write-concern! WriteConcern/JOURNAL_SAFE)

;; ;; 4. Get database instance
;; (def mydb (m/get-db conn "mongo-spike"))

;; ;; test data
;; (def data ["leon" "trotsky"])
;; ;; test fn
;; (def cap '(fn [i] (map #(clojure.string/capitalize %) i)))
;; ;; insert fn into mongo (this will create the db, if it doesn't already exist)
;; (defn insert-fn [f] (col/insert mydb "fns" {:name (name 'f) :body (prn-str f)}))
;; ;; retrieve fn
;; (defn retrieve-fn [f] (:body (col/find-one-as-map mydb "fns" {:name (name 'f)})))

;; ;; evaluate function using test data
;; (defn eval-fn [f arg] ((load-string f) arg))

;; This is another approach, for single words
;;(defn eval-fn [f arg] ((-> f symbol resolve) arg))

;; Execute these functions to test it out...
;; (insert-fn cap)
;; (def x (retrieve-fn cap))
;; (eval-fn x data)


