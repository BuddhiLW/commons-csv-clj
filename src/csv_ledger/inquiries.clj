(ns csv-ledger.inquiries
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:gen-class))

(def my-vec [1 2 "foo" "bar"])
(.isDirectory (io/file "data"))

;; Created a csv with some content
(with-open [writer (io/writer "out-file.csv")]
  (csv/write-csv writer
                 [["abc" "def"]
                  ["ghi" "jkl"]]))

(with-open [wrtr (io/writer "./foo.txt")]
  (doseq [i my-vec]
    (.write wrtr (str i "\n"))))
