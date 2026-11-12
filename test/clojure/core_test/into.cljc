(ns clojure.core-test.into
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists clojure.core/into

  ;; DOCSTRING:
  ;; clojure.core/into
  ;; []
  ;; [to]
  ;; [to from]
  ;; [to xform from]
  ;; Returns a new coll consisting of to with all of the items of
  ;; from conjoined. A transducer may be supplied.
  ;; (into x) returns x. (into) returns [].

  ;; Note: the transducers used here are limited to `map`, `filter` and
  ;; `take`.

  (deftest test-into
    (testing "arity 0"
      ;; (into) returns []
      ;; note we have to do both of these checks since '() = []
      (is (= [] (into)))
      (is (vector? (into))))

    (testing "arity 1"
      ;; (into x) returns x
      (is (= [] (into [])))
      (is (= [1 2 3] (into [1 2 3])))
      (is (vector? (into [1 2 3])))
      (is (= '(1 2 3) (into '(1 2 3))))
      (is (list? (into '(1 2 3))))
      (is (= {:a 1} (into {:a 1})))
      (is (= #{1 2} (into #{1 2})))
      (is (nil? (into nil)))
      ;; `x` itself is returned, not a copy of it
      (let [v [1 2 3]]
        (is (identical? v (into v))))
      (let [m {:a 1}]
        (is (identical? m (into m)))))

    (testing "arity 2"
      ;; Items of `from` are conjoined onto `to` one at a time, so
      ;; vectors grow at the end while lists grow at the front
      (are [expected to] (= expected (into to [1 2]))
        [1 2]    []
        [3 1 2]  [3]
        '(2 1 3) '(3)
        #{1 2}   #{}
        '(2 1)   nil)

      ;; Any collection/reducible works as `from`
      (is (= [1 2 3 4] (into [1 2] '(3 4))))
      (is (= [0 1 2 3 4] (into [] (range 5))))
      (is (= [1 2 3] (into [] (sorted-set 3 1 2))))
      (is (= [1 2 3] (into [1 2] (map identity [3]))))

      ;; Map entries are conjoined as map entries, so they can be
      ;; collected into a map or into a vector
      (are [expected to from] (= expected (into to from))
        {:a 1 :b 2} {}          [[:a 1] [:b 2]]
        {:a 1 :b 2} {:a 1}      {:b 2}
        {:a 1 :b 2} {:a 0 :b 2} {:a 1}
        {:a 1 :b 2} {}          (sorted-map :b 2 :a 1)
        [[:a 1]]    []          {:a 1})

      ;; An empty or `nil` `from` leaves `to` unchanged
      (is (= [] (into [] [])))
      (is (= [1 2] (into [1 2] [])))
      (is (= [1 2] (into [1 2] nil)))
      (is (= {} (into {} {})))
      (is (nil? (into nil [])))

      ;; Sorted collections stay sorted
      (let [m (into (sorted-map) [[:b 2] [:a 1]])
            s (into (sorted-set) [3 1 2])]
        (is (= {:a 1 :b 2} m))
        (is (sorted? m))
        (is (= #{1 2 3} s))
        (is (sorted? s)))

      ;; Metadata of `to` is preserved
      (let [m {:foo 42}]
        (is (= m (meta (into (with-meta [] m) [1 2]))))
        (is (= m (meta (into (with-meta {} m) {:a 1}))))
        (is (= m (meta (into (with-meta #{} m) [1]))))
        (is (= m (meta (into (with-meta '() m) [1])))))

      ;; Metadata of the `from` items is preserved. `into` conjoins the
      ;; items themselves, so metadata riding along on them makes it
      ;; into `to` untouched.
      (let [m {:foo 42}
            item (with-meta [:a 1] m)]
        (is (= m (meta (first (into [] [item])))))
        (is (= m (meta (first (into '() [item])))))
        (is (= m (meta (first (into nil [item])))))
        (is (= m (meta (first (into #{} [item])))))
        ;; ...whatever kind of collection `from` is
        (is (= m (meta (first (into [] (vals {:k item}))))))
        (is (= m (meta (first (into [] #{item})))))
        (is (= m (meta (first (into [] (map identity [item]))))))
        ;; ...and when a map entry itself carries metadata
        (is (= m (meta (first (into [] [(with-meta [:k :v] m)])))))))

    (testing "arity 3, with a transducer"
      (is (= [2 3 4] (into [] (map inc) [1 2 3])))
      (is (= [1 3] (into [] (filter odd?) [1 2 3 4])))

      ;; The transducer supplies the conjoined items, so it works with
      ;; every kind of `to`
      (is (= {:a 2 :b 3} (into {} (map (fn [[k v]] [k (inc v)])) {:a 1 :b 2})))
      (is (= {1 1, 2 2} (into {} (map (fn [x] [x x])) [1 2])))
      (is (= #{2 3} (into #{} (map inc) [1 2])))
      (is (= '(3 2) (into '() (map inc) [1 2])))
      (is (= [1 3 4] (into [1] (map inc) [2 3])))

      ;; An empty or `nil` `from` produces no items at all
      (is (= [] (into [] (map inc) [])))
      (is (= [] (into [] (map inc) nil)))
      (is (= [] (into [] (filter odd?) nil)))
      (is (= [1 2] (into [1 2] (map inc) [])))

      ;; An infinite `from` is fine: the reduction pulls only as many
      ;; items as the transducer asks for, so `take` is what stops it
      (is (= [0 1] (into [] (take 2) (range))))
      (is (= [0 1 2] (into [] (take 3) (range))))

      ;; Metadata of `to` is preserved when a transducer is supplied
      (let [m {:foo 42}]
        (is (= m (meta (into (with-meta [] m) (map inc) [1 2]))))
        (is (= m (meta (into (with-meta {} m) (map identity) {:a 1}))))
        (is (= m (meta (into (with-meta #{} m) (map inc) [1]))))
        (is (= m (meta (into (with-meta '() m) (filter odd?) [1]))))
        ;; ...for sorted collections too
        (is (= m (meta (into (with-meta (sorted-map) m) (map identity) {:a 1}))))
        (is (= m (meta (into (with-meta (sorted-set) m) (map inc) [1]))))
        ;; ...and when the transducer emits nothing at all
        (is (= m (meta (into (with-meta [] m) (map inc) []))))
        (is (= m (meta (into (with-meta [] m) (filter odd?) [2 4])))))

      ;; A transducer is handed the original `from` items, and the
      ;; items it emits keep their metadata too
      (let [m {:foo 42}
            item (with-meta [:a 1] m)]
        (is (= m (meta (first (into [] (map identity) [item])))))
        (is (= m (meta (first (into [] (filter vector?) [item])))))))

    (testing "invalid arguments"
      ;; `to` must be a collection or `nil`
      (is (p/thrown? (into 1 [1])))
      (is (p/thrown? (into "abc" [1])))
      (is (p/thrown? (into :a [1])))
      ;; Elements of `from` must be map entries when `to` is a map
      (is (p/thrown? (into {:a 1} [1])))
      ;; `xform` must be a transducer
      (is (p/thrown? (into [] nil [1])))
      ;; `from` must be reducible/seqable
      (is (p/thrown? (into [] (map inc) 5))))))
