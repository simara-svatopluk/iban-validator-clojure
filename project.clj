(defproject iban-validator-clojure "0.1.0-SNAPSHOT"
  :description "Demonstration that Clojure is actually useful"
  :url "https://github.com/simara-svatopluk/iban-validator-clojure"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler iban-validator-clojure.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}}
  :repl-options {:init-ns iban-validator-clojure.core})
