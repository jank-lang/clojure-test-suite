(ns clojure.core-test.remove-watch
  (:require [clojure.test :as t :refer [deftest is testing]]
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists]]))

(when-var-exists remove-watch
  (deftest test-remove-watch
    (testing "remove watch atoms"
      (let [messages (volatile! #{})
            watcher1 (fn [key ref old new]
                       (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher1}))
            watcher2 (fn [key ref old new]
                       (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher2}))
            watchable (atom 0)
            update! (fn [] (swap! watchable inc))]
        ;; Make sure messages is empty
        (is (empty? @messages))

        ;; Add watches
        (add-watch watchable :key1 watcher1)
        (add-watch watchable :key2 watcher2)

        ;; Update the atom
        (update!)

        ;; Check if all the watchers fired and added messages correctly
        (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                           {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}}))

        ;; Remove a watch
        (remove-watch watchable :key1)

        ;; Update again
        (update!)

        ;; Check if the right watchers disappeared
        (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                           {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}
                           {:key :key2 :ref watchable :old 1 :new 2 :watcher :watcher2}}))

        ;; Remove the last watch
        (remove-watch watchable :key2)

        ;; Update again
        (update!)

        ;; Check to make sure nothing was added
        (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                           {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}
                           {:key :key2 :ref watchable :old 1 :new 2 :watcher :watcher2}}))))

    #?(:cljs
       nil

       :default
       (testing "remove watch vars"
         (def watchable 0)

         (let [messages (volatile! #{})
               watcher1 (fn [key ref old new]
                          (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher1}))
               watcher2 (fn [key ref old new]
                          (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher2}))
               update! (fn [] (alter-var-root #'watchable inc))]
           ;; Make sure messages is empty
           (is (empty? @messages))

           ;; Add watches
           (add-watch #'watchable :key1 watcher1)
           (add-watch #'watchable :key2 watcher2)

           ;; Update the atom
           (update!)

           ;; Check if all the watchers fired and added messages correctly
           (is (= @messages #{{:key :key1 :ref #'watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref #'watchable :old 0 :new 1 :watcher :watcher2}}))

           ;; Remove a watch
           (remove-watch #'watchable :key1)

           ;; Update again
           (update!)

           ;; Check if the right watchers disappeared
           (is (= @messages #{{:key :key1 :ref #'watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref #'watchable :old 0 :new 1 :watcher :watcher2}
                              {:key :key2 :ref #'watchable :old 1 :new 2 :watcher :watcher2}}))

           ;; Remove the last watch
           (remove-watch #'watchable :key2)

           ;; Update again
           (update!)

           ;; Check to make sure nothing was added
           (is (= @messages #{{:key :key1 :ref #'watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref #'watchable :old 0 :new 1 :watcher :watcher2}
                              {:key :key2 :ref #'watchable :old 1 :new 2 :watcher :watcher2}})))))

    #?(:cljs
       nil

       :default
       (testing "remove watch refs"
         (let [messages (volatile! #{})
               watcher1 (fn [key ref old new] (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher1}))
               watcher2 (fn [key ref old new] (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher2}))
               watchable (ref 0)
               update! (fn [] (dosync (alter watchable inc)))]
           ;; Make sure messages is empty
           (is (empty? @messages))

           ;; Add watches
           (add-watch watchable :key1 watcher1)
           (add-watch watchable :key2 watcher2)

           ;; Update the atom
           (update!)

           ;; Check if all the watchers fired and added messages correctly
           (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}}))

           ;; Remove a watch
           (remove-watch watchable :key1)

           ;; Update again
           (update!)

           ;; Check if the right watchers disappeared
           (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}
                              {:key :key2 :ref watchable :old 1 :new 2 :watcher :watcher2}}))

           ;; Remove the last watch
           (remove-watch watchable :key2)

           ;; Update again
           (update!)

           ;; Check to make sure nothing was added
           (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}
                              {:key :key2 :ref watchable :old 1 :new 2 :watcher :watcher2}})))))

    #?(:cljs
       nil

       :default
       (testing "remove watch refs"
         (let [messages (volatile! #{})
               watcher1 (fn [key ref old new]
                          ;; `await` does a `send` on the agent, so
                          ;; only add message if old and new differ
                          (when (not= old new)
                            (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher1})))
               watcher2 (fn [key ref old new]
                          ;; `await` does a `send` on the agent, so
                          ;; only add message if old and new differ
                          (when (not= old new)
                            (vswap! messages conj {:key key :ref ref :old old :new new :watcher :watcher2})))
               watchable (agent 0)
               update! (fn []
                         (send watchable inc)
                         (await watchable))]
           ;; Make sure messages is empty
           (is (empty? @messages))

           ;; Add watches
           (add-watch watchable :key1 watcher1)
           (add-watch watchable :key2 watcher2)

           ;; Update the atom
           (update!)

           ;; Check if all the watchers fired and added messages correctly
           (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}}))

           ;; Remove a watch
           (remove-watch watchable :key1)

           ;; Update again
           (update!)

           ;; Check if the right watchers disappeared
           (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}
                              {:key :key2 :ref watchable :old 1 :new 2 :watcher :watcher2}}))

           ;; Remove the last watch
           (remove-watch watchable :key2)

           ;; Update again
           (update!)

           ;; Check to make sure nothing was added
           (is (= @messages #{{:key :key1 :ref watchable :old 0 :new 1 :watcher :watcher1}
                              {:key :key2 :ref watchable :old 0 :new 1 :watcher :watcher2}
                              {:key :key2 :ref watchable :old 1 :new 2 :watcher :watcher2}})))))))
