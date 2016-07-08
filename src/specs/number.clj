(ns specs.number
  (:require [clojure.spec :as s]
            [clojure.test.check.generators :as gen])
  (:import [java.math BigDecimal]))

(defn decimal-in
  "Specs a decimal number. The number type may be anything that bigdec
   accepts. Options:

    :precision - the number of digits in the unscaled value (default none)
    :scale     - the number of digits to the right of the decimal (default none)
    :min       - minimum value (inclusive, default none)
    :max       - maximum value (inclusive, default none)"
  [& options]
  (let [{:keys [precision scale min max]} options]
    (when (and min max)
      (assert (>= max min)))
    (when precision
      (assert (pos? precision)))
    (when scale
      (assert (not (neg? scale))))
    (when (and precision scale)
      (>= precision scale))
    (letfn [(pred [d]
              (try
                (let [d (bigdec d)]
                  (and (or (not precision)
                           (>= precision (.precision d)))
                       (or (not scale)
                           (let [d-scale (.scale d)]
                             (and (not (neg? d-scale))
                                  (>= scale d-scale))))
                       (or (not min)
                           (>= d min))
                       (or (not max)
                           (>= max d))))
                (catch Exception _ false)))
            (gen []
              (gen/let [p (gen/double* {:infinite? false :NaN? false :min min :max max})]
                (cond-> (bigdec p)
                  scale
                  (.setScale scale BigDecimal/ROUND_HALF_UP))))]
      (when min
        (assert (pred min)))
      (when max
        (assert (pred max)))
      (s/spec pred :gen gen))))