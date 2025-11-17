(ns clojure.core-test.mapcat
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists mapcat
  (deftest test-mapcat
    (testing "nil input"
      (is (nil? (seq (mapcat identity nil)))))
    (testing "concatenation"
      (is (= [1 2 3 4] (mapcat identity [[1 2] '(3 4)]))))
    (testing "function producing seqs"
      (is (= [0 0 1 0 1 2] (mapcat #(range %) [1 2 3]))))
    (testing "empty results contribute nothing"
      (is (= [2] (mapcat (fn [x] (if (odd? x) [] [x])) [1 2 3]))))
    (testing "strings"
      (is (= [\a \b \c] (mapcat identity ["ab" "" "c"]))))
    (testing "as transducer"
      (is (= [1 1 2 2 3 3] (transduce (mapcat #(repeat 2 %)) conj [] [1 2 3]))))
    (testing "into with transducer"
      (is (= [0 0 1 1 2 2] (into [] (mapcat #(repeat 2 %)) (range 3)))))
    (testing "infinite input laziness"
      (is (= [0 0 1 1 2]  (take 5  (mapcat #(repeat 2 %) (range))))))
    (testing "empty collection input"
      (is (= [] (mapcat identity []))))
    (testing "single element producing empty sequence"
      (is (= [] (mapcat (constantly []) [42]))))
    (testing "single element producing seq"
      (is (= [99] (mapcat list [99]))))
    (testing "function returns a string (seqable)"
      (is (= [\h \i] (mapcat identity ["hi"]))))
    (testing "flatten key/value pairs"
      (is (= [:a 1 :b 2 :c 3] (mapcat identity {:a 1 :b 2 :c 3}))))
    (testing "function returns nil"
      (is (= [] (mapcat (constantly nil) [1 2 3]))))
    (testing "two collections zipped, function applied to pairs"
      (is (= [2 2 4 4 6 6] (mapcat (fn [x y] [(* 2 x) (* 2 y)]) [1 2 3] [1 2 3]))))
    (testing "one collection shorter than the other"
      (is (= [2 4] (mapcat (fn [x y] [(* x y)]) [1 2] [2 2 2]))))
    (testing "works lazily on infinite input"
      (is (= [0 1 2 3 4 5] (->> (mapcat (fn [x] [x]) (range)) (take 6)))))
    (testing "function sometimes returns []"
      (is (= [1 3 5] (mapcat #(if (odd? %) [%] []) (range 1 6)))))
    (testing "function sometimes returns nil"
      (is (= [2 4] (mapcat #(if (even? %) [%] nil) (range 1 5)))))
    (testing "multiple collections"
      (is (= '(:a 1 :b 2 :c 3) (mapcat list [:a :b :c] [1 2 3]))))
    (testing "incorrect shape"
      (is (thrown? #?(:cljs :default :default Exception)
                   (mapcat identity 5))))
    (testing "non-seqable second arg"
      (is (thrown? #?(:cljs :default :default Exception)
                   (mapcat identity 5))))
    (testing "non-function first arg"
      (is (thrown? #?(:cljs :default :default Exception)
                   (mapcat 42 [1 2]))))
    (testing "non-concatable return value"
      (is (thrown? #?(:cljs :default :default Exception)
                   (doall (mapcat identity (range 2))))))))
