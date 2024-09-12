(ns iban-validator-clojure.core_test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [iban-validator-clojure.core-spec :refer :all]
            [iban-validator-clojure.core :refer :all]
            )
  (:import (clojure.lang ExceptionInfo)))

(deftest iban-validation
  (testing "Valid IBAN checksum"
    (is (true? (valid-iban-checksum? "CZ9050513587633843814896")))
    (is (true? (valid-iban-checksum? "NL18ABNA1041035373")))
    )
  (testing "Invalid IBAN checksum"
    (is (false? (valid-iban-checksum? "CZ9950513587633843814896")))
    (is (false? (valid-iban-checksum? "NX18ABNA1041035373")))
    )
  )

(deftest iban-validation-with-bic
  (let [
        test-banks (xml-file-to-map (io/resource "banks.xml"))
        test-iban-structure (xml-file-to-map (io/resource "iban-structure.xml"))
        test-find-bic (partial find-bic
                               (map-country-to-fields test-iban-structure)
                               (map-country-to-bank-details test-banks)
                               )
        test-validate-iban (partial validate-iban test-find-bic)
        ]

   (testing "Invalid IBAN checksum"
    (is (= {:valid false :reason :checksum} (test-validate-iban "CZ9950513587633843814896")))
    )
  (testing "Invalid BIC"
    (is (= {:valid false :reason :bic} (test-validate-iban "CZ7407100000001234567899")))
    )
  (testing "Valid"
    (is (= {:valid true :bic "YOURNL2AXXX"} (test-validate-iban "NL91YOUR9000000009")))
    )))

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
    (testing "short IBAN"
      (is (= nil (test-find-bic "xx"))))
    (testing "unknown country"
      (is (= nil (test-find-bic "XX7407100000001234567899"))))
    )
  )

(deftest spec-test
  (testing boolean-to-int
    (is (thrown? ExceptionInfo (boolean-to-int 10))))
  (testing char-to-int
    (is (thrown? ExceptionInfo (char-to-int 10))))
  (testing valid-iban-checksum?
    (is (thrown? ExceptionInfo (valid-iban-checksum? 10))))
  (testing iban-country
    (is (thrown? ExceptionInfo (iban-country 10))))
  )
