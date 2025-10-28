(ns clojure.core-test.while
  (:require [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists while
  (deftest test-while
    (testing "executes while condition is true"
      (let [a (atom 0)]
        (while (< @a 5)
          (swap! a inc))
        (is (= 5 @a))))

    (testing "returns nil"
      (let [a (atom 0)]
        (is (nil? (while (< @a 3)
                    (swap! a inc))))))

    (testing "never executes if condition starts false"
      (let [a (atom 0)]
        (while false
          (swap! a inc))
        (is (= 0 @a))))

    (testing "multiple expressions in body"
      (let [a (atom 0)
            b (atom 0)]
        (while (< @a 3)
          (swap! a inc)
          (swap! b + @a))
        (is (= 3 @a))
        (is (= 6 @b))))

    (testing "with countdown"
      (let [a (atom 10)]
        (while (> @a 0)
          (swap! a dec))
        (is (= 0 @a))))

    (testing "nested while"
      (let [a (atom 0)
            b (atom 0)]
        (while (< @a 3)
          (reset! b 0)
          (while (< @b 2)
            (swap! b inc))
          (swap! a inc))
        (is (= 3 @a))
        (is (= 2 @b))))

    (testing "with side effects only"
      (let [results (atom [])]
        (while (< (count @results) 3)
          (swap! results conj (count @results)))
        (is (= [0 1 2] @results))))

    (testing "condition checked each iteration"
      (let [a (atom 0)
            counter (atom 0)]
        (while (do (swap! counter inc) (< @a 3))
          (swap! a inc))
        (is (= 4 @counter))))

    (testing "with complex condition"
      (let [a (atom 0)
            b (atom 10)]
        (while (and (< @a 5) (> @b 5))
          (swap! a inc)
          (swap! b dec))
        (is (= 5 @a))
        (is (= 5 @b))))

    (testing "accumulator pattern"
      (let [sum (atom 0)
            i (atom 1)]
        (while (<= @i 5)
          (swap! sum + @i)
          (swap! i inc))
        (is (= 15 @sum))))

    (testing "with break condition via atom"
      (let [a (atom 0)
            continue (atom true)]
        (while @continue
          (swap! a inc)
          (when (>= @a 3)
            (reset! continue false)))
        (is (= 3 @a))))

    (testing "with empty body still checks condition"
      (let [a (atom 0)]
        (while (do (swap! a inc) (< @a 3)))
        (is (= 3 @a))))

    (testing "condition must be reevaluated"
      (let [x (atom 5)]
        (while (pos? @x)
          (swap! x dec))
        (is (= 0 @x))))

    (testing "with collection building"
      (let [result (atom [])
            i (atom 0)]
        (while (< @i 5)
          (swap! result conj @i)
          (swap! i inc))
        (is (= [0 1 2 3 4] @result))))

    (testing "infinite loop prevention with counter"
      (let [counter (atom 0)
            max-iterations 1000]
        (while (< @counter max-iterations)
          (swap! counter inc))
        (is (= max-iterations @counter))))

    (testing "with string building"
      (let [s (atom "")
            i (atom 0)]
        (while (< @i 3)
          (swap! s str @i)
          (swap! i inc))
        (is (= "012" @s))))

    (testing "factorial calculation"
      (let [n 5
            result (atom 1)
            i (atom 1)]
        (while (<= @i n)
          (swap! result * @i)
          (swap! i inc))
        (is (= 120 @result))))))
