(ns clojure.core-test.number-range)

#?(:jank (cpp/raw "#include <limits>"))

(def ^:const max-int #?(:clj Long/MAX_VALUE
                        :cljr Int64/MaxValue
                        :cljs js/Number.MAX_SAFE_INTEGER
                        :jank (cpp/value "std::numeric_limits<jank::i64>::max()")
                        :default 0x7FFFFFFFFFFFFFFF))

(def ^:const min-int #?(:clj Long/MIN_VALUE
                        :cljr Int64/MinValue
                        :cljs js/Number.MIN_SAFE_INTEGER
                        :jank (cpp/value "std::numeric_limits<jank::i64>::min()")
                        :default 0x8000000000000000))

(def ^:const all-ones-int #?(:cljs 0xFFFFFFFF
                             :default -1))

(def ^:const full-width-checker-pos #?(:cljs 0x55555555
                                       :default 0x5555555555555555))

(def ^:const full-width-checker-neg #?(:cljs 0xAAAAAAAA
                                       :default -0x5555555555555556))

(def ^:const max-double #?(:clj Double/MAX_VALUE
                           :cljr Double/MaxValue
                           :cljs js/Number.MAX_VALUE
                           :jank (cpp/value "std::numeric_limits<jank::f64>::max()")
                           :default 1.7976931348623157e+308))

(def ^:const min-double #?(:clj Double/MIN_VALUE
                           :cljr Double/Epsilon ; NOTE: definitely not Double/MinValue -- ouch!
                           :cljs js/Number.MIN_VALUE
                           :jank (cpp/value "std::numeric_limits<jank::f64>::min()")
                           :default 4.9e-324))

