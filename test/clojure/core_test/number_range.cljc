(ns clojure.core-test.number-range)

;; TODO jank support blocked on https://github.com/jank-lang/jank/issues/195

(def ^:const max-int #?(:clj Long/MAX_VALUE
                        :cljs js/Number.MAX_SAFE_INTEGER
                        :default 0x7FFFFFFFFFFFFFFF))

(def ^:const min-int #?(:clj Long/MIN_VALUE
                        :cljs js/Number.MIN_SAFE_INTEGER
                        :default 0x8000000000000000))

(def ^:const all-ones-int #?(:cljs 0xFFFFFFFF
                             :default -1))

(def ^:const all-zeros 0)

(def ^:const full-width-checker-pos #?(:cljs 0x55555555
                                       :default 0x5555555555555555))

(def ^:const full-width-checker-neg #?(:cljs 0xAAAAAAAA
                                       :default -0x5555555555555556))

(def ^:const max-double #?(:clj Double/MAX_VALUE
                           :cljs js/Number.MAX_VALUE
                           :default 1.7976931348623157e+308))

(def ^:const min-double #?(:clj Double/MIN_VALUE
                           :cljs js/Number.MIN_VALUE
                           :default 4.9e-324))

