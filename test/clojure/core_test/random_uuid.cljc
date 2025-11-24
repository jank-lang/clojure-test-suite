(ns clojure.core-test.random-uuid
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]
            [clojure.string :as str]))

(when-var-exists random-uuid

  (defn has-format-4?
    [uuid]
    ;; Check that the first digit of the third group is a 4, signaling
    ;; UUID format 4 (random)
    (= \4 (-> uuid
              str
              (str/split #"-")
              (get-in [2 0]))))
  
  (deftest test-random-uuid
    ;; Strategy: generate a number of random UUIDs.
    ;; 1. Ensure they are unique
    ;; 2. Ensure they are all UUIDs
    (let [uuids (repeatedly 10 random-uuid)]
      ;; Should all be UUIDs
      (is (every? uuid? uuids))
      ;; Should all be unique
      (is (= (count uuids) (count (set uuids))))
      ;; Should all have UUID format 4
      (is (every? has-format-4? uuids)))))
