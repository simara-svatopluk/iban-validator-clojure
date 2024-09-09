(ns iban-validator-clojure.core)

(require '[clojure.xml :as xml] '[clojure.string :as string])
(import java.io.ByteArrayInputStream)

(defn boolean-to-int [boolean]
  (if boolean 1 0))

(defn char-to-int [c]
  (cond
    (<= (int \0) (int c) (int \9)) (- (int c) (int \0))
    :else (+ (- (int c) (int \A)) 10)
    )
  )

(defn valid-iban-checksum? [input]
  (->> (concat (drop 4 input) (take 4 input))
       (map (comp str char-to-int))
       (apply str)
       bigint
       (#(rem % 97))
       (= 1)
       )
  )


(defn iban-country [iban]
  (apply str (take 2 iban)))

(defn has-tag? [tag input-map]
  (-> input-map
      :tag
      (= tag)
      ))

(def has-country-tag? (partial has-tag? :country))
(def has-iban-fields-tag? (partial has-tag? :iban-fields))
(def has-bank-code? (partial has-tag? :bank-code))
(def has-bic? (partial has-tag? :bic))

(defn row-value [f row]
  (->> row
       (filter f)
       first
       :content
       first
       ))

(def row-country (partial row-value has-country-tag?))
(def row-iban-fields (partial row-value has-iban-fields-tag?))
(def row-bank-code (partial row-value has-bank-code?))
(def row-bic (partial row-value has-bic?))

(defn map-country-to-fields [xml]
  (->> xml
       :content
       (map :content)
       (map #(identity [(row-country %1) (row-iban-fields %1)]))
       (into {})
       ))

(defn row-bank-code-bic-pair [row] [(row-bank-code row) (row-bic row)])

(defn bank-group-to-map [group]
  (->> group
       (map row-bank-code-bic-pair)
       (into {}))
  )

(defn map-country-to-bank-details [xml]
  (->> xml
       :content
       (map :content)
       (group-by row-country)
       (#(update-vals % bank-group-to-map))
       ))

(defn xml-file-to-map [filename]
  (-> (slurp filename)
      .getBytes
      ByteArrayInputStream.
      xml/parse
      )
  )

(defn iban-fields-to-bank-mask [bank-fields]
  (->> bank-fields
       seq
       (map (partial = \b))
       (map boolean-to-int)
       ))

(defn iban-to-ints [iban]
  (map int (seq iban))
  )

(defn extract-bank-code [iban-fields iban]
  (->> (map *
            (iban-fields-to-bank-mask iban-fields)
            (iban-to-ints iban)
            )
       (filter (complement zero?))
       (map char)
       (apply str)
       )
  )

(defn find-bic [map-of-country-to-fields map-of-country-to-bank-details iban]
  (let [country (iban-country iban)
        iban-fields (map-of-country-to-fields country)
        map-of-banks (map-of-country-to-bank-details country)]
    (when (every? some? (list country iban-fields map-of-banks))
      (->> (extract-bank-code iban-fields iban)
           map-of-banks
           )
      )
    ))

(defn validate-iban [find-bic-fn iban]
  (cond
    (valid-iban-checksum? iban) (let [bic (find-bic-fn iban)]
                                  (cond
                                    (nil? bic) {:valid false :reason :bic}
                                    :else {:valid true :bic bic}
                                    )
                                  )
    :else {:valid false :reason :checksum}
    )
  )