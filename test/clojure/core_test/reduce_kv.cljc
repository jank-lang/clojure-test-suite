(ns clojure.core-test.reduce-kv
  (:require
   [clojure.test :as t :refer [deftest is are testing]]
   [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists reduce-kv
  ;; Docstring:
  ;; Reduces an associative collection. f should be a function of 3
  ;; arguments. Returns the result of applying f to init, the first key
  ;; and the first value in coll, then applying f to that result and the
  ;; 2nd key and value, etc. If coll contains no entries, returns init
  ;; and f is not called. Note that reduce-kv is supported on vectors,
  ;; where the keys will be the ordinals.

  (deftest test-reduce-kv
    (testing "Reduces an associative collection."
      (is (p/thrown? (reduce-kv (fn [_ _ _]) nil #{:foo})))
      (is (p/thrown? (reduce-kv (fn [_ _ _]) nil '(:foo))))
      (is (p/thrown? (reduce-kv (fn [_ _ _]) nil :foo)))
      (is (p/thrown? (reduce-kv (fn [_ _ _]) nil 'foo)))
      (is (p/thrown? (reduce-kv (fn [_ _ _]) nil "foo"))))

    #?(:cljs
       (testing "f should be a function of 3 arguments"
         (is (p/thrown? (reduce-kv nil ::init {:foo :bar})))
         (is (= ::called (reduce-kv (fn [_ _ _] ::called) ::init {:foo :bar})))
         (is (p/thrown? (reduce-kv nil ::init [:foo])))
         (is (= ::called (reduce-kv (fn [_ _ _] ::called) ::init [:foo]))))
       :default
       (testing "f should be a function of 3 arguments"
         (is (p/thrown? (reduce-kv nil ::init {:foo :bar})))
         (is (p/thrown? (reduce-kv (fn []) ::init {:foo :bar})))
         (is (p/thrown? (reduce-kv (fn [_]) ::init {:foo :bar})))
         (is (p/thrown? (reduce-kv (fn [_ _]) ::init {:foo :bar})))
         (is (= ::called (reduce-kv (fn [_ _ _] ::called) ::init {:foo :bar})))
         (is (p/thrown? (reduce-kv (fn [_ _ _ _]) ::init {:foo :bar})))
         (is (p/thrown? (reduce-kv nil ::init [:foo])))
         (is (p/thrown? (reduce-kv (fn []) ::init [:foo])))
         (is (p/thrown? (reduce-kv (fn [_]) ::init [:foo])))
         (is (p/thrown? (reduce-kv (fn [_ _]) ::init [:foo])))
         (is (= ::called (reduce-kv (fn [_ _ _] ::called) ::init [:foo])))
         (is (p/thrown? (reduce-kv (fn [_ _ _ _]) ::init [:foo])))))

    (testing "Returns the result of applying f to init..."
      (is (= {:k :v 0 :foo 1 :bar}
             (reduce-kv assoc {:k :v} (vector :foo :bar))))
      (is (= {:k :v :foo :bar :beep :boop}
             (reduce-kv assoc {:k :v} (array-map :foo :bar :beep :boop))))
      (is (= {:k :v :foo :bar :beep :boop}
             (reduce-kv assoc {:k :v} (hash-map :foo :bar :beep :boop))))
      (when-var-exists sorted-map
        (is (= {:k :v :foo :bar :beep :boop}
               (reduce-kv assoc {:k :v} (sorted-map :foo :bar :beep :boop))))))

    (testing "If coll contains no entries, returns init and f is not called."
      (is (= ::init (reduce-kv (fn [_ _ _] (throw (ex-info "not called" {}))) ::init nil)))
      (is (= ::init (reduce-kv (fn [_ _ _] (throw (ex-info "not called" {}))) ::init {})))
      (is (= ::init (reduce-kv (fn [_ _ _] (throw (ex-info "not called" {}))) ::init [])))

      (is (= ::init (reduce-kv nil ::init nil)))
      (is (= ::init (reduce-kv nil ::init {})))
      (is (= ::init (reduce-kv nil ::init []))))

    (testing "reduce-kv is supported on vectors, where the keys will be the ordinals."
      (is (= [::init 0 1]
             (reduce-kv (fn [acc i v] [acc i v]) ::init [1])))
      (is (= [45 100]
             (reduce-kv (fn [[i' v'] i v] [(+ i' i) (+ v' v)])
                        [0 0]
                        (vec (repeat 10 10))))))

    (testing "reduced is supported"
      (doseq [coll [{:foo :bar :beep :boop} [:foo :bar]]]
        (let [calls (atom 0)]
          (is (= ::reduced
                 (reduce-kv (fn [_ _ _]
                              (swap! calls inc)
                              (reduced ::reduced))
                            ::init coll)))
          (is (= 1 @calls)))))

    (testing "Some examples from https://clojuredocs.org/clojure.core/reduce-kv"
      (testing "Swap keys and values in a map"
        (is (= {1 :a, 2 :b, 3 :c}
               (reduce-kv #(assoc %1 %3 %2) {} {:a 1 :b 2 :c 3}))))
      (testing "It works with indexes on vectors as well"
        (is (= {2 "three", 1 "two", 0 "one"}
               (reduce-kv (fn [res idx itm] (assoc res idx itm)) {} ["one" "two" "three"]))))
      (testing "You can define map-kv using reduce-kv"
        (letfn [(map-kv [f coll]
                  (reduce-kv (fn [m k v] (assoc m k (f v))) (empty coll) coll))]
          (is (= {:c 3, :b 20, :a 13} (map-kv inc {:a 12, :b 19, :c 2})))
          (is (= [2 2 3 4 6] (map-kv inc [1 1 2 3 5])))))
      (testing "update-map-entries"
        (letfn [(update-map-entries [m e]
                  (reduce-kv (fn [r k v] (assoc  r k v))  m e))]
          (is (= {:a 5, :b 9, :c 3}
                 (update-map-entries {:a 1 :b 2 :c 3} {:a 5 :b 9})))
          (is (= {:a 5, :b 9, :c 3, :d 8}
                 (update-map-entries {:a 1 :b 2 :c 3} {:a 5 :b 9 :d 8}))))))))
