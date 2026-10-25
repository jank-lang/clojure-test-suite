(ns clojure.core-test.destructure
  "Test destructure examples found at https://clojure.org/guides/destructuring"
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists destructure
  (deftest test-destructure
    (testing "Sequential Destructuring"
      (testing "vector"
        (let [[x y z] [1 2 3]]
          (is (= 1 x))
          (is (= 2 y))
          (is (= 3 z))))

      (testing "list"
        (let [[x y z] '(1 2 3)]
          (is (= 1 x))
          (is (= 2 y))
          (is (= 3 z))))

      (testing "string"
        (let [[x y z] "abc"]
          (is (= \a x))
          (is (= \b y))
          (is (= \c z))))

      (testing "small list"
        (let [[a b c d e f g] '(1 2 3)]
          (is (= 1 a))
          (is (= 2 b))
          (is (= 3 c))
          (is (nil? d))
          (is (nil? e))
          (is (nil? f))
          (is (nil? g))))

      (testing "large list"
        (let [[a b c] '(1 2 3 4 5 6 7 8 9 10)]
          (is (= 1 a))
          (is (= 2 b))
          (is (= 3 c))))

      (testing "pick args"
        (let [[item1 & remaining] ["Michael" "Amber" "Aaron" "Nick" "Earl" "Joe"]]
          (is (= "Michael" item1))
          (is (= ["Amber" "Aaron" "Nick" "Earl" "Joe"] remaining)))
        (let [[item1 _ item3 _ item5 _] ["Michael" "Amber" "Aaron" "Nick" "Earl" "Joe"]]
          (is (= "Michael" item1))
          (is (= "Aaron" item3))
          (is (= "Earl" item5))))

      (testing "all"
        (let [[x & remaining :as all] [1 2 3 4 5]]
          (is (= 1 x))
          (is (= [2 3 4 5] remaining))
          (is (= [1 2 3 4 5] all)))
        (let [[x & remaining :as all] "Clojure"]
          (is (= \C x))
          (is (= '(\l \o \j \u \r \e) remaining))
          (is (= "Clojure" all))))

      (testing "nested"
        (let [[[x1 y1 :as group1] [x2 y2 :as group2]] [[5 10] [15 20]]]
          (is (= 5 x1))
          (is (= 10 y1))
          (is (= 15 x2))
          (is (= 20 y2))
          (is (= [5 10] group1))
          (is (= [15 20] group2)))))

    (testing "Associative Destructuring"
      (let [data {:name "Super Co."
                  :location "Philadelphia"
                  :description "The worldwide leader in plastic tableware."}
            {name :name
             location :location
             description :description
             category :category
             :or {category "Category not found"}
             :as all} data]
        (is (= (:name data) name))
        (is (= (:location data) location))
        (is (= (:description data) description))
        (is (= "Category not found" category))
        (is (= data all)))

      (let [{category :category} {}] (is (nil? category)))

      (testing "keys"
        (let [data {:name "Super Co."
                    :location "Philadelphia"
                    :description "The worldwide leader in plastic tableware."}
              {:keys [name location description]} data]
          (is (= (:name data) name))
          (is (= (:location data) location))
          (is (= (:description data) description))))

      (testing "strs"
        (let [data {"first-name" "Joe" "last-name" "Smith"}
              {:strs [first-name last-name]} data]
          (is (= (data "first-name") first-name))
          (is (= (data "last-name") last-name))))

      (testing "syms"
        (let [data {'first-name "Jane" 'last-name "Doe"}
              {:syms [first-name last-name]} data]
          (is (= (data 'first-name) first-name))
          (is (= (data 'last-name) last-name))))

      (testing "nested"
        (let [nested-map {:a 1, :nested {:aa 10}}
              {:keys [a b]
               :as m
               :or {a 42 b 3}
               {:keys [aa bb] :or {bb 30}} :nested}
              nested-map]
          (is (= 1 a))
          (is (= 3 b))
          (is (= nested-map m))
          (is (= 10 aa))
          (is (= 30 bb)))))

    (testing "Keyword arguments"
      (is (= {:a 1} ((fn [& {:as options}] options) :a 1)))

      ((fn configure [_ & {:keys [debug verbose]
                           :or {debug false, verbose false}
                           :as opts}]
         (is (false? debug))
         (is (false? verbose))
         (is (nil? opts)))
       10)

      ((fn configure [_ & {:keys [debug verbose]
                           :or {debug false, verbose false}
                           :as opts}]
         (is (true? debug))
         (is (false? verbose))
         (is (= {:debug true} opts)))
       5 :debug true)

      ((fn configure [_ & {:keys [debug verbose]
                           :or {debug false, verbose false}
                           :as opts}]
         (is (true? debug))
         (is (true? verbose))
         (is (= {:debug true :verbose true} opts)))
       12 :verbose true :debug true)

      (testing "trailing maps"
        ((fn configure [_ & {:keys [debug verbose]
                             :or {debug false, verbose false}
                             :as opts}]
           (is (true? debug))
           (is (true? verbose))
           (is (= {:debug true :verbose true} opts)))
         12 {:verbose true :debug true})

        ((fn configure [_ & {:keys [debug verbose]
                             :or {debug false, verbose false}
                             :as opts}]
           (is (true? debug))
           (is (true? verbose))
           (is (= {:debug true :verbose true} opts)))
         12 :debug true {:verbose true})))

    (testing "Namespaced keywords"
      (let [human {:person/name "Franklin"
                   :person/age 25
                   :hobby/hobbies "running"}
            {:keys [hobby/hobbies]
             :person/keys [name age]
             :or {age 0}} human]
        (is (= (:person/name human) name))
        (is (= (:person/age human) age))
        (is (= (:hobby/hobbies human) hobbies)))

      (let [human {:person/name "Franklin"
                   :person/age 25
                   :hobby/name "running"}
            {:person/keys [age]
             hobby-name :hobby/name
             person-name :person/name} human]
        (is (= (:person/name human) person-name))
        (is (= (:person/age human) age))
        (is (= (:hobby/name human) hobby-name))))))