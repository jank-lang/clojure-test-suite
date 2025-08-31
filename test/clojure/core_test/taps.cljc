(ns clojure.core-test.taps
  (:require [clojure.test :as t :refer [deftest testing is are]]
            #?(:cljs [cljs.test :refer-macros [async]])
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer)  [when-var-exists]]))

(when-var-exists clojure.core/add-tap
  #?(:cljs
     (do
       (defn tap-tester
         [atom-ref]
         (fn [x]
           (if (fn? x)
             (x nil)
             (swap! atom-ref conj x))))

       (defn await-tap
         []
         (let [promise-obj (js/Promise.withResolvers)
               promise (.-promise promise-obj)
               resolve (.-resolve promise-obj)]
           (tap> resolve)
           promise))

       (defn p-delay
         "This is safer in JS given tap> uses setTimeout 0 instead of a thread"
         [ms]
         (let [{:keys [promise resolve]} (js->clj (js/Promise.withResolvers)
                                                  :keywordize-keys true)]
           (js/setTimeout resolve ms)
           promise))

       (deftest tapping
         (async
          done
          (let [data-ref-1 (atom [])
                data-ref-2 (atom [])
                tester-1 (tap-tester data-ref-1)
                tester-2 (tap-tester data-ref-2)]

            (-> (js/Promise.resolve)
                (.then (fn []
                         (tap> 0)
                         (p-delay 2)))
                (.then (fn [] (is (nil? (add-tap tester-1)))))
                (.then (fn []
                         (tap> 0)
                         (tap> 1)
                         (await-tap)))
                (.then (fn []
                         (is (nil? (add-tap tester-2)))))
                (.then (fn []
                         (tap> 2)
                         (await-tap)))
                (.then (fn []
                         (remove-tap tester-1)))
                (.then (fn []
                         (tap> 3)
                         (await-tap)))
                (.then (fn []
                         (is (= [0 1 2] @data-ref-1))
                         (is (= [2 3] @data-ref-2))))
                (.then done done))))))

     :default
     (do
       (defn tap-tester
         [atom-ref]
         (fn [x]
           (if (instance? clojure.lang.IPending x)
             (deliver x nil)
             (swap! atom-ref conj x))))

       (defn await-tap
         []
         (let [p (promise)]
           (tap> p)
           @p))

       (deftest tapping
         (let [data-ref-0 (atom [])
               data-ref-1 (atom [])
               data-ref-2 (atom [])
               tester-0 (tap-tester data-ref-0)
               tester-1 (tap-tester data-ref-1)
               tester-2 (tap-tester data-ref-2)]
           ;; setup
           (add-tap tester-0)
           (tap> 0) ;; we check that this 0 isn't seen by the other tap fns
           (await-tap) ;; we wait since tap> above is async
           ;; end setup
           (is (nil? (add-tap tester-1)))
           (tap> 0)
           (tap> 1)
           (await-tap)
           (is (nil? (add-tap tester-2)))
           (tap> 2)
           (await-tap)
           (remove-tap tester-1)
           (tap> 3)
           (await-tap)
           (is (= [0 1 2] @data-ref-1))
           (is (= [2 3] @data-ref-2)))))))

     ;; can test in the next iteration of the event loop, but this won't show up in the testing results
