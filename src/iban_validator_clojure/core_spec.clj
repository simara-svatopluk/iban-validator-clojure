(ns iban-validator-clojure.core-spec
  (:require [iban-validator-clojure.core :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            )
  )

(s/fdef iban-validator-clojure.core/boolean-to-int
        :args (s/cat :boolean boolean?)
        :ret int?)
(stest/instrument 'iban-validator-clojure.core/boolean-to-int)

(s/fdef iban-validator-clojure.core/char-to-int
        :args (s/cat :input char?)
        :ret int?)
(stest/instrument 'iban-validator-clojure.core/char-to-int)

(s/fdef iban-validator-clojure.core/valid-iban-checksum?
        :args (s/cat :input string?)
        :return boolean?)
(stest/instrument 'iban-validator-clojure.core/valid-iban-checksum?)

(defn min-length? [min-length input] (>= (count input) min-length))
(def iban? (s/and string? (partial min-length? 2)))

(s/fdef iban-validator-clojure.core/iban-country
        :args (s/cat :iban iban?)
        :return string?)
(stest/instrument 'iban-validator-clojure.core/iban-country)
