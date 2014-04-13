(ns org.tobereplaced.http-accept-headers-test
  (:require [clojure.test :refer [deftest is]]
            [org.tobereplaced.http-accept-headers
             :refer [parse-accept
                     parse-accept-charset
                     parse-accept-encoding
                     parse-accept-language
                     accept-wildcards
                     accept-language-wildcards]]))

(deftest parse-accept-test
  (let [wildcards (accept-wildcards "text/html" "application/json")
        html ["text/html" {}] json ["application/json" {}]]
    (is (= [html] (parse-accept wildcards nil))
        "nil should return only default")
    (is (= [html] (parse-accept wildcards ""))
        "an empty string should return only default")
    (is (= [html] (parse-accept wildcards "*"))
        "* should return only default")
    (is (= [html] (parse-accept wildcards "*/*"))
        "*/* should return only default")
    (is (= [html] (parse-accept wildcards "text/*"))
        "text/* should return text/html")
    (is (= [json] (parse-accept wildcards "application/*"))
        "application/* should return application/json")
    (is (= [json html] (parse-accept wildcards "text/html;q=0.5, application/json"))
        "quality parameter should default to 1.")
    (is (= [html json]
           (parse-accept wildcards "text/*;q=0.7, application/json; q=0.1"))
        "quality parameter should be honored in complex case")
    (is (= [["funny/weird" {"bar" "33"}] html]
           (parse-accept wildcards "funny/weird;bar=33, *;q=0.5"))
        "should handle arbitrary types and pass through parameters")))

(deftest parse-accept-charset-test
  (is (= ["utf-8"] (parse-accept-charset "utf-8" nil))
      "nil should return only default")
  (is (= ["utf-8"] (parse-accept-charset "utf-8" ""))
      "an empty string should return only default")
  (is (= ["utf-8"] (parse-accept-charset "utf-8" "*"))
      "* should return only default")
  (is (= ["iso-8859-1" "funny" "utf-8"]
         (parse-accept-charset "utf-8" "funny;q=0.7, utf-8;q=0.5"))
      "quality parameter should be honored")
  (is (= ["utf-8" "iso-8859-1" "funny"]
         (parse-accept-charset "utf-8" "utf-8, funny;q=0.5"))
      "no * or iso-8859-1 should include iso-8859-1 after other q=1 values")
  (is (= ["utf-8"] (parse-accept-charset "utf-8" "utf-8, iso-8859-1;q=0"))
      "should be able to explicitly exclude iso-8859-1"))

(deftest parse-accept-encoding-test
  (is (= ["identity"] (parse-accept-encoding "gzip" nil))
      "nil should return only identity")
  (is (= ["identity"] (parse-accept-encoding "gzip" ""))
      "an empty string should return only identity")
  (is (= ["gzip"] (parse-accept-encoding "gzip" "*"))
      "* should return only default")
  (is (= ["compress" "gzip" "identity"]
         (parse-accept-encoding "gzip" "gzip;q=0.7, compress"))
      "quality parameter should be honored with identity last")
  (is (= ["gzip"] (parse-accept-encoding "gzip" "gzip, identity;q=0"))
      "should be able to explicitly exclude identity")
  (is (= ["gzip"] (parse-accept-encoding "gzip" "gzip, *;q=0"))
      "should be able to explicitly exclude identity"))

(deftest parse-accept-language-test
  (let [wildcards (accept-language-wildcards "en-us" "xx-yy")]
    (is (= ["en-us"] (parse-accept-language wildcards nil))
        "nil should return only default")
    (is (= ["en-us"] (parse-accept-language wildcards ""))
        "an empty string should return only default")
    (is (= ["en-us"] (parse-accept-language wildcards "*"))
        "* should return only default")
    (is (= ["en-us"] (parse-accept-language wildcards "en"))
        "en should return en-us")
    (is (= ["xx-yy"] (parse-accept-language wildcards "xx"))
        "xx should return xx-yy")
    (is (= ["xx-yy"] (parse-accept-language wildcards "xx"))
        "xx should return xx-yy")
    (is (= ["aa-bb" "xx-yy" "en-us"]
           (parse-accept-language wildcards "xx;q=0.5, *;q=0.2, aa-bb"))
        "quality parameter should be honored in complex case")))
