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
  (partial find-bic (map-country-to-fields iban-structure) (map-country-to-bank-details banks)))

(def validate-iban-runtime
  (partial validate-iban find-bic-runtime))


(defn find-iban-handler [iban]
  (let [result (find-bic-runtime iban)]
    (cond result {
                  :headers {"Content-Type" "text/plain"}
                  :body    result
                  }
          :else {
                 :status  400
                 :headers {"Content-Type" "text/plain"}
                 :body    "BIC not found"
                 })))

(defn reason-to-string [reason]
  (cond (= reason :checksum) "Invalid checksum"
        (= reason :bic) "Bank not found in out DB"
        :else ""
        )
  )

(defn validate-iban-handler [iban]
  (let [result (validate-iban-runtime iban)]
    (cond (true? (result :valid)) {
                                   :headers {"Content-Type" "text/html"}
                                   :body    (concat "VALID IBAN <br> BIC: " (result :bic))
                                   }
          :else {
                 :headers {"Content-Type" "text/html"}
                 :body    (concat "INVALID IBAN <br> reason: " (reason-to-string (result :reason)))
                 }
          )
    ))

(defroutes app-routes
           (GET "/" [] (io/resource "index.html"))
           (GET "/find-bic/:iban" [iban] (find-iban-handler iban))
           (GET "/validate/:iban" [iban] (validate-iban-handler iban))
           (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
