(ns task.test2
  (:use [clojure.test]))

(with-test
  (defn foo [])
  (is (= 2 (+ 1 1)))
  (is (= 3 (+ 1 1))))
