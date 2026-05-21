(ns clojure.core-test.number-range
  #?@(:lpy [(:import sys)]
      :default []))

(def ^:const max-int #?(:cljr Int64/MaxValue
                        :phel php/PHP_INT_MAX
                        :jank (#cpp (:member (std.numeric_limits jank.i64) max))
                        :cljs js/Number.MAX_SAFE_INTEGER
                        :clj Long/MAX_VALUE
                        :default 0x7FFFFFFFFFFFFFFF))

(def ^:const min-int #?(:cljr Int64/MinValue
                        :phel php/PHP_INT_MIN
                        :jank (#cpp (:member (std.numeric_limits jank.i64) min))
                        :cljs js/Number.MIN_SAFE_INTEGER
                        :clj Long/MIN_VALUE
                        :default -0x8000000000000000))

(def ^:const all-ones-int #?(:cljs 0xFFFFFFFF
                             :default -1))

(def ^:const full-width-checker-pos #?(:cljs 0x55555555
                                       :default 0x5555555555555555))

(def ^:const full-width-checker-neg #?(:cljs 0xAAAAAAAA
                                       :default -0x5555555555555556))

(def ^:const max-double #?(:cljr Double/MaxValue
                           :lpy (.-max sys/float-info)
                           :phel php/PHP_FLOAT_MAX
                           :jank (#cpp (:member (std.numeric_limits jank.f64) max))
                           :cljs js/Number.MAX_VALUE
                           :clj Double/MAX_VALUE
                           :default 1.7976931348623157e+308))

(def ^:const min-double #?(:cljr Double/Epsilon ; NOTE: definitely not Double/MinValue -- ouch!
                           :lpy (.-min sys/float_info)
                           :phel php/PHP_FLOAT_MIN
                           :jank (#cpp (:member (std.numeric_limits jank.f64) min))
                           :cljs js/Number.MIN_VALUE
                           :clj Double/MIN_VALUE
                           :default 4.9e-324))

