(ns csv-ledger.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:gen-class))

(defn -main
  "Call on <<Faturamento.csv>> and <<VALORES_VIORC.csv>>;
  Return \"ledger.csv\", which informs the situation of the balance."
  [spenditure-table raw-table]
  (println "Hello, World!"))

(def opened-revenue (csv/read-csv (io/reader "data/Faturamento.csv")))
(def header-revenue (first opened-revenue))
(def raw-revenue (drop 1 opened-revenue))


(defn read-column [filename column-index]
  (with-open [reader (io/reader filename)]
    (let [data (csv/read-csv reader)]
      (map #(nth % column-index) data))))

(defn sum-second-column [filename]
  (->> (read-column filename 1)
       (drop 1) ;; Drop header column
       (map #(Double/parseDouble %))
       (reduce + 0)))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
	  (rest csv-data)))

(def vec-revenues (vec (csv-data->maps opened-revenue)))

(def unique-CNPJ (set (map :CNPJ (csv-data->maps opened-revenue))))

(defn submap? [a b]
"return true or false"
  (every? (fn [[k v]] (= v (b k)))  a))

(defn cnpj#
  "take a =CNPJ-value= and narrow down to the subclass which has this CNPJ"
  [CNPJ-value vec-revenues]
  (filter #(submap? {:CNPJ CNPJ-value} %) vec-revenues))

(cnpj# "17194123000103" vec-revenues)

(def opened-biorc (csv/read-csv (io/reader "data/VALORES_BIORC.csv")))
(def vec-biorc (vec (csv-data->maps opened-biorc)))

(defn submap-cond#
  "take a =CNPJ-value= and narrow down to the subclass which has this CNPJ"
  [key value vec-revenues]
  (filter #(submap? {(keyword key) value} %) vec-revenues))

(defn cpf-cnpj#
  [value vec-revenues]
    (submap-cond# "CPFCNPJCLI" value vec-revenues))

(def nu-value (cpf-cnpj# (nth (vec unique-CNPJ) 6) vec-biorc))

(defn sum-a-numeric-key
  [key coll]
  (reduce + (map bigdec (map (keyword key) coll))))

(defn cash-flow
  "Calculate the cash-flow for a given cnpj-client"
  [cnpj vec-revenues vec-biorc]
  (let [nu-value-revenue (cnpj# cnpj vec-revenues)
        nu-value-spent (cpf-cnpj# cnpj vec-biorc)]
    (- (+ (bigdec ((keyword "FATURAMENTO") (first nu-value-revenue)))
          (sum-a-numeric-key "TOTAL_RECEBIDO" nu-value-spent))
       (sum-a-numeric-key "A_RECEBER" nu-value-spent))))

(defn cash-flow-zip
  "zip the CNPJ-key with the cash-flow-value in key-value pairs"
  [unique-CNPJ-set]
  (zipmap
   (map keyword
        (map #(nth (vec unique-CNPJ-set) %)
             (range (count unique-CNPJ-set))))
   (take (count unique-CNPJ-set)
         (map #(cash-flow % vec-revenues vec-biorc)
              unique-CNPJ-set))))
