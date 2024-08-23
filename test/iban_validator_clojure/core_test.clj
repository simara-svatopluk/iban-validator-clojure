(ns iban-validator-clojure.core_test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [iban-validator-clojure.core :refer :all]))

(deftest feature
  (let [
        test-banks (xml-file-to-map (io/resource "banks.xml"))
        test-iban-structure (xml-file-to-map (io/resource "iban-structure.xml"))
        test-find-bic (partial find-bic
                               (map-country-to-fields test-iban-structure)
                               (map-country-to-bank-details test-banks)
                               )
        ]
    (testing "IBAN to BIC from database"
      (is (= "FIOBCZPPXXX" (test-find-bic "CZ6920100000001234567899")))
      (is (= "FIOBCZPPXXX" (test-find-bic "CZ7020100000001234567899")))
      (is (= "YOURNL2AXXX" (test-find-bic "NL91YOUR9000000009")))
      )
    (testing "Bank code not found in DB"
      (is (= nil (test-find-bic "CZ7407100000001234567899")))
      )
    )
  )

(deftest edge-cases
  (let [
        test-banks (xml-file-to-map (io/resource "banks.xml"))
        test-iban-structure (xml-file-to-map (io/resource "iban-structure.xml"))
        test-find-bic (partial find-bic
                               (map-country-to-fields test-iban-structure)
                               (map-country-to-bank-details test-banks)
                               )
        ]
    (testing "Too short IBAN"
      (is (= nil (test-find-bic ""))))
    (testing "Invalid inputs"
      (is (= nil (test-find-bic nil)))
      (is (= nil (test-find-bic {})))
      (is (= nil (test-find-bic #{}))))
    (testing "Unknown country"
      (is (= nil (test-find-bic "XX7407100000001234567899"))))
    )
  )
