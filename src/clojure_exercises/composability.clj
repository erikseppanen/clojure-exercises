(ns clojure-exercises.composability)

;; From Stuart Sierra's 'Thinking in Data' talk

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Make your Functions Composable
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Bad (Not Composable)
#_(defn complex-process []
    (let [a (get-component @global-state)
          b (subprocess-one a)
          c (subprocess-two a b)
          d (subprocess-three a b c)]
      (reset! global-state d)))

;; Good (Composable)
#_(defn complex-process [state]
    (-> state
        subprocess-one
        subprocess-two
        subprocess-three))

;; Isolate Computation from State
#_(swap! an-atom complex-process)        ; atom
#_(dosync (alter a-ref complex-process)) ; ref
#_(send an-agent complex-process)        ; agent

;; Isolate Components
#_(defn complex-process [state]
    (-> state
        (update-in [:part-a] subprocess-a)
        (update-in [:part-b] subprocess-b)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Isolate Side Effects
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Bad
#_(defn complex-process [state]
    (let [result (computation state)]
      (if (condition? result)
        (launch-missile)
        (erase-hard-drive))))

;; Better
#_(defn complex-process [state]
    (assoc state :analysis (computation state)))

#_(defn decision [state]
    (assoc state :response
           (if (condition? (:analysis state))
             :launch-missile
             :erase-hard-drive)))

#_(defn defend-nation [state]
    (case (:response state)
      :launch-missile (launch-missile)
      :erase-hard-drive (erase-hard-drive)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Naming Conventions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; A function which...       | has a name that is... | returns...
;;----------------------------------------------------------------
;; Computes a value          | noun: what it returns | value
;; Creates a new thing       | noun: what it creates | value
;; Gets input from the world | "get-noun"            | value
;; Affects the world         | verb: what it does    | nil
