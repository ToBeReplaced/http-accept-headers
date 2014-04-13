# http-accept-headers #

Parse HTTP Accept headers in accordance with RFC 2616 (in Clojure).

## About ##

This library intends to be 100% compliant with RFC 2616. It is
web-framework agnostic and simply provides a way to go from various
Accept headers to sequences of their values.

Parsers are implemented are Accept, Accept-Charset, Accept-Encoding,
and Accept-Language. In all cases, you may specify what you would like
to use in place of wildcard values.

Each parser returns a sequence of values corresponding to the header
that can be used to provide an appropriate response. The sequence is
ordered as per RFC 2616 sections 14.1-14.4, honoring the quality
parameter and all other required comparisons.

It is assumed that any default values you pass in place of wildcards
will be acceptible to your server, so they are not expanded into all
possible combinations.

This library assumes that the server does not have a preference when
it receives more than one field with the same quality value. If the
server would like to have control over this, a different workflow is
required."

## Why? ##

I like standards.  I get upset when I find out that developers don't
honor them, and more upset when I find that the tools to honor them
aren't available.  I looked at the many Clojure web frameworks and
routing libraries, and no one provided raw access to how to handle
varying Accept headers.

This is a core library to help make your website standards compliant.
If you start to face problems of being in-the-large like
internationalization, this will work with you instead of get in your
way.

## Supported Clojure Versions ##

http-accept-headers is tested on Clojure 1.6.0 only.

## Maturity ##

This is alpha quality software.

## Installation ##

http-accept-headers is available as a Maven artifact from [Clojars]:

```clojure
[org.tobereplaced/http-accept-headers "0.1.0"]
```

http-accept-headers follows [Semantic Versioning].  Please note that
this means the public API for this library is not yet considered
stable.

## Documentation ##

Please read the [Codox API Documentation].  The [unit tests] may also
be of interest.

## Support ##

Please post any comments, concerns, or issues to the Github issues
page or find me on `#clojure`.  I welcome any and all feedback.

## Changelog ##

### v0.1.0 ###

- Initial Release

## License ##

Copyright © 2014 ToBeReplaced

Distributed under the Eclipse Public License, the same as Clojure.
The license can be found at LICENSE in the root of this distribution.

[Codox API Documentation]: http://ToBeReplaced.github.com/http-accept-headers
[unit tests]: https://github.com/tobereplaced/http-accept-headers/blob/master/test/org/tobereplaced/http_accept_headers_test.clj
[Clojars]: http://clojars.org/org.tobereplaced/http-accept-headers
[Semantic Versioning]: http://semver.org
