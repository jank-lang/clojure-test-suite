(ns clojure.core-test.update
  (:require clojure.core
            [clojure.test :as t :refer [deftest testing is are]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists
 clojure.core/update
 (deftest test-update
   (testing "maps"
     (are [in ex] (= (apply update in) ex)
       [{:k 5} :k identity]      {:k 5}
       [{:k 5} :k inc]           {:k 6}
       [{:k 5} :k (partial * 3)] {:k 15}

       ;; Multi args
       [{:k 5} :k * 3]           {:k 15}
       [{:k 5} :k * 3 4]         {:k 60}
       [{:k 5} :k * 3 4 5]       {:k 300}
       [{:k 5} :k * 3 4 5 6]     {:k 1800}
       [{:k 5} :k * 3 4 5 6 7]   {:k 12600}

       ;; Keyed item is the first arg in multi call
       [{:k 5} :k - 3]           {:k 2}
       [{:k 5} :k - 3 4]         {:k (- 5 3 4)}
       [{:k 5} :k - 3 4 5]       {:k (- 5 3 4 5)}
       [{:k 5} :k - 3 4 5 6]     {:k (- 5 3 4 5 6)}
       [{:k 5} :k - 3 4 5 6 7]   {:k (- 5 3 4 5 6 7)}

       ;; `update` will create `:not-found` keys, and pass `nil`
       [{:a 5} :k identity]      {:k nil,  :a 5}
       [{:a 5} :k nil?]          {:k true, :a 5}
       [{}     :k identity]      {:k nil}
       [{}     :k nil?]          {:k true}
       [nil    :k identity]      {:k nil}
       [nil    :k nil?]          {:k true}

       ;; Can work with non-keyword keys
       [{nil 1} nil inc]         {nil 2}
       [{nil 1} nil inc]         {nil 2}
       [{{} 1}  {}  inc]         {{} 2}
       [{[] 1}  []  inc]         {[] 2}
       [{dec 1} dec inc]         {dec 2}
       [{"" 1}  ""  inc]         {"" 2}
       [{0 1}   0   inc]         {0 2}

       ;; Can work with non-functions
       [{:k 5} :k :f]           {:k nil}
       [{:k 5} :k #{}]          {:k nil}
       [{:k 5} :k {}]           {:k nil}
       [nil    :k :f]           {:k nil}
       [nil    :k #{}]          {:k nil}
       [nil    :k {}]           {:k nil}

       ;; CLJS can accept arbitrary arguments
       ;; While CLJ will Throw (See last test case)
       #?@(:cljs
           ([{:k 1} :k inc 1 2 3 4] {:k 2}))

       ;; Update Vector inside Map
       [{:a [0 1 2], :b 4} :a conj 3] {:a [0 1 2 3], :b 4}))

   (testing "vectors"
     (are [in ex] (= (apply update in) ex)
       ;; Normal access and update
       [[0 1] 0 identity]      [0 1]
       [[0 1] 0 inc]           [1 1]
       [[0 1] 1 identity]      [0 1]
       [[0 1] 1 inc]           [0 2]

       ;; Adds/appends at the end of the vector, function recieves nil
       [[0 1] 2 identity]       [0 1 nil]
       [[0 1] 2 nil?]           [0 1 true]

       ;; Keyed item is the first arg in multi call
       [[1 3 9] 0 - 3]          [(- 1 3) 3 9]
       [[1 3 9] 0 - 3 4]        [(- 1 3 4)       3 9]
       [[1 3 9] 0 - 3 4 5]      [(- 1 3 4 5)     3 9]
       [[1 3 9] 0 - 3 4 5 6]    [(- 1 3 4 5 6)   3 9]
       [[1 3 9] 0 - 3 4 5 6 7]  [(- 1 3 4 5 6 7) 3 9]

       ;; Can work with non-functions
       [[] 0 #{}]               [nil]
       [[] 0  {}]               [nil]
       [[] 0  :f]               [nil]))

   (testing "Throws"
     (are [in] (thrown? #?(:clj Exception :cljs js/Error) (apply update in))
       ;; Throw when settting index 1 when 0 doesn't exist
       [[]        1 identity]
       ;; Throw on Negative indices!
       [[0 1]    -1 identity]
       ;; Throw when keyword on vector
       [[1 2 3]  :k identity]
       ;; Throw on non-Associatives
       [[1 2 3]    :k identity]
       [1          :k identity]
       ["hi"       :k identity]
       ['()        :k identity]
       ['(1 2 3)   :k identity]

       ;; Throw when passing a val instead of function
       [{:k 5} :k 1]
       [{:k 5} :k '()]
       [{:k 5} :k '()]
       [{:k 5} :k []]
       [{:k 5} :k nil]
       [{:k 5} :k ""]

       ;; Laziness doesn't work on CLJS
       #?(:clj [(repeat 1) :k identity])
       ;; Throw when wrong number of indices are passed to the function
       ;; CLJS returns 1, and doesn't throw!
       #?(:clj [{:k 1} :k identity 1 2 3 4])))))
