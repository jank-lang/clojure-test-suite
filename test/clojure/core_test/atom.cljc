(ns clojure.core-test.atom
  (:require clojure.core
            [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists clojure.core/atom
  (deftest test-atom
    (testing "What happens when the input is nil?"
      (let [nil-atm  (atom nil)
            nil-atm2 (atom nil nil nil)
            nil-atm3 (apply atom (take 11 (repeat nil)))]
        #?(:cljs (is (every? #(satisfies? cljs.core/IAtom %) [nil-atm nil-atm2 nil-atm3]))
           :clj (is (every? (partial instance? clojure.lang.Atom) [nil-atm nil-atm2 nil-atm3])))
        (is (every? nil? (map deref [nil-atm nil-atm2 nil-atm3])))))

    (testing "metadata"
      (are [v m] (let [the-atom (atom v :meta m)]
                   (and (= v (deref the-atom))
                        (= m (meta the-atom))))
        ;; metadata can be a map or nil
        nil nil
        nil {}
        nil {:foo "foo"})
      (when-var-exists clojure.core/sorted-map
        (is (= {:a "a"} (meta (atom nil :meta (sorted-map :a "a"))))))
      (when-var-exists clojure.core/array-map
        (is (= {:a "a"} (meta (atom nil :meta (array-map :a "a"))))))
      (when-var-exists clojure.core/hash-map
        (is (= {:a "a"} (meta (atom nil :meta (hash-map :a "a"))))))
      #?(:cljs (is (= 5 (meta (atom nil :meta 5)))),
         :default (is (thrown? Exception (atom nil :meta 5))))
      #?(:cljs (is (= #{} (meta (atom nil :meta #{})))),
         :default (is (thrown? Exception (atom nil :meta #{}))))
      #?(:cljs (is (= [] (meta (atom nil :meta (vector))))),
         :default (is (thrown? Exception (atom nil :meta (vector))))))

    (testing "validator-fn"        
      ;; Docstring: "If the new state is unacceptable, the validate-fn should
      ;; return false or throw an exception." Read this as _logical_ false,
      ;; including `nil` as well as `false`:
      (testing "all new vals permitted with nil validator"
        (let [atm (atom {} :validator nil)]
          (is (= {} (deref atm)))
          (is (= {:foo "foo"} (swap! atm assoc :foo "foo")))
          (is (= {:foo "foo"} (deref atm)))
          (is (= {"bar" :bar} (reset! atm {"bar" :bar})))
          (is (= {"bar" :bar} (deref atm)))
          (is (= 3 (reset! atm 3)))
          (is (= 3 (deref atm)))
          (is (= nil (reset! atm nil)))
          (is (= nil (deref atm)))))

      (testing "all new vals permitted with always-truthy validator"
        (let [atm (atom {} :validator (constantly true))]
          (is (= {} (deref atm)))
          (is (= {:foo "foo"} (swap! atm assoc :foo "foo")))
          (is (= {:foo "foo"} (deref atm)))
          (is (= {"bar" :bar} (reset! atm {"bar" :bar})))
          (is (= {"bar" :bar} (deref atm)))
          (is (= 3 (reset! atm 3)))
          (is (= 3 (deref atm))))

        (let [atm (atom {} :validator (constantly :some-val))]
          (is (= {} (deref atm)))
          (is (= {:foo "foo"} (swap! atm assoc :foo "foo")))
          (is (= {:foo "foo"} (deref atm)))
          (is (= {"bar" :bar} (reset! atm {"bar" :bar})))
          (is (= {"bar" :bar} (deref atm)))
          (is (= 3 (reset! atm 3)))
          (is (= 3 (deref atm)))))

      (testing "always-falsey validator can't initialize atom"
        #?(:cljs (testing "Broken but current behavior due to CLJS-3447"
                   ;; FIXME when https://clojure.atlassian.net/browse/CLJS-3447 is fixed
                   (is (= {} (deref (atom {} :validator (constantly nil)))))),
           :default (is (thrown? Exception (atom {} :validator (constantly nil)))))
        #?(:cljs (testing "Broken but current behavior due to CLJS-3447"
                   ;; FIXME when https://clojure.atlassian.net/browse/CLJS-3447 is fixed
                   (is (= {} (deref (atom {} :validator (constantly false)))))),
           :default (is (thrown? Exception (atom {} :validator (constantly false)))))
        #?(:cljs (testing "Broken but current behavior due to CLJS-3447"
                   ;; FIXME when https://clojure.atlassian.net/browse/CLJS-3447 is fixed
                   (is (= {} (deref (atom {} :validator #(when true (throw (ex-info "boom" {})))))))),
           :default (is (thrown? Exception (atom {} :validator #(when true (throw (ex-info "boom" {}))))))))

      (testing "conditional validators are obeyed at creation, swap! and reset!"
        #?(:cljs (testing "Broken but current behavior due to CLJS-3447"
                   ;; FIXME when https://clojure.atlassian.net/browse/CLJS-3447 is fixed
                   (is (= #{} (deref (atom #{} :validator (fn [v] (some string? v))))))),
           :default (is (thrown? Exception (atom #{} :validator (fn [v] (some string? v))))))
        (let [some-strings (atom #{"str"} :validator (fn [v] (some string? v)))]
          (is (= #{"str" :not-a-string} (swap! some-strings conj :not-a-string)))
          (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                       (swap! some-strings disj "str")))
          (is (= #{"str"} (swap! some-strings disj :not-a-string)))
          (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                       (reset! some-strings #{})))
          (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                       (reset! some-strings :neither-string-nor-set)))
          (is (= #{"str"} (deref some-strings)))
          (is (= #{"some other string"} (reset! some-strings #{"some other string"})))
          (is (= #{"some other string"} (deref some-strings))))
        
        (let [all-strings (atom #{} :validator (fn [v] (every? string? v)))]
          (is (= #{"str"} (swap! all-strings conj "str")))
          (is (= #{} (swap! all-strings disj "str")))
          (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                       (reset! all-strings :neither-string-nor-set)))
          (is (thrown? #?(:cljs :default :clj Exception :cljr Exception)
                       (reset! all-strings #{:not-a-string})))
          (is (= #{"new string"} (reset! all-strings #{"new string"})))
          (is (= #{"new string"} (deref all-strings))))))

    (testing "metadata and validator function together"
      (let [m {:foo "foo"}
            vf even?
            the-atom (atom 0 :validator vf :meta m)
            the-atom2 (atom 0 :meta m :validator vf)]
        (is (= vf (get-validator the-atom)))
        (is (= m (meta the-atom)))
        (is (= vf (get-validator the-atom2)))
        (is (= m (meta the-atom2)))))

    (testing "atom accepts all values"
      (are [v] (let [the-atom (atom v)]
                 (and (= v (deref the-atom))
                      #?(:cljs (is (satisfies? cljs.core/IAtom the-atom))
                         :clj (is (instance? clojure.lang.Atom the-atom)))))
        'sym
        `sym
        "string"
        1
        1.0
        #?(:cljs "cljs is the only (?) Clojure dialect that doesn't support ratios"
           :default 111/7)
        \newline
        nil
        true
        false
        ##Inf
        :kw
        :ns/kw
        '(one two three)
        [1 2 3]
        {:k "value"}
        (zipmap (take 100 (range))
                (cycle ['foo 'bar 'baz 'qux]))
        (set (range 0 334 3)))
      (testing "infinite sequence"
        (let [r (range)]
          (is (= r (deref (atom r)))))))))
