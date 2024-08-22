(ns iban-validator-clojure.handler
  (:require [clojure.java.io :as io]
            [iban-validator-clojure.core :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            ))

(def banks
  (xml-file-to-map (io/resource "banks.xml")))

(def iban-structure
  (xml-file-to-map (io/resource "iban-structure.xml")))

(def find-bic-runtime
  (partial find-bic (map-country-to-fields iban-structure) (map-country-to-bank-details banks))
  )

(defn find-bic-handler [iban]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (find-bic-runtime iban)}
  )

(defroutes app-routes
           (GET "/" [] (io/resource "index.html"))
           (GET "/find-bic/:iban" [iban] (find-bic-handler iban))
           (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
