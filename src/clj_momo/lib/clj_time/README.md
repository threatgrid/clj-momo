## clm-momo's clj-time

This is a facade over clj-time.core and cljs-time.core that also adds
some additional features.  This facade exists because clj-time and
cljs-time duplicate the same protocols, but we want to be able to
share the code between CLJ and CLJS code.  This library hides the
messy details of dealing with different time library implementations
so that the rest of our code can be cleaner.

### A warning

You should be a little bit careful using this library.  Not all of the
features in either delegated library are implemented.  Although every
attempt was made to write this bug-free, some functions are neither
tried not tested.  If you depend on some code here, please double
check that there are tests for your feature.  If there are no tests,
you should consider adding some to this library.

### Support for Date objects

This library adds extra support for java.util.Date and js/Date.  We
also tend to work with Date objects a lot.

### Internal Dates

There is an extra concept in this library called 'internal-date'.  An
instance of 'internal-date' is just a date-time object that represents
the opinions of the team developing clj-momo.  It is a name for the
set of opinions that we hold, with the intention of making it easier
to work with dates.

Here are the opinions that are we try to honor:

 * In Clojure code, we mostly use java.util.Date objects
 * In ClojureScript code, we mostly use goog.date.UtcDateTime objects
 * When working with strings, we use ISO8601 format
 * Our date-time code shouldn't be a total mess
   * When messy code is unavoidable, it should be contained within this library

### Examples:

There are a lot of examples in the tests.
