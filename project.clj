(defproject clojure-exercises "0.1.0-SNAPSHOT"
  :description "A project for clojure exercises."
  :repositories [["conjars" "http://conjars.org/repo/"]
                 ["sonatype-oss-public"
                  "https://oss.sonatype.org/content/groups/public/"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.generators "0.1.2"]
                 [com.novemberain/monger "2.0.1"]
                 [org.mongodb/mongo-java-driver "2.12.3"]
                 [incanter "1.9.0"]     ; pre-2.0
                 ;; [incanter "1.5.5"]
                 [instaparse "1.4.1"]
                 [com.velisco/herbert "0.6.6"]
                 [prismatic/schema "0.3.3"]
                 [clj-time "0.8.0" :exclusions [org.clojure/clojure]]
                 [clj-diff "1.0.0-SNAPSHOT"]]
  :jvm-opts ["-Xmx3g"]
  :main exercise.core
  :repl-options {:init-ns user}
  :global-vars {*print-length* 100}
  :deploy-branches ["master"]
  :profiles {:dev {:resource-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.8"]]}
             :uberjar {:aot :all}})
