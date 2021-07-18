
<img src="./docs/logo.png" title="newrelic-clj" width="300" height="300" align="left" padding="5px"/>
<small>
<br/><br/><br/><br/>
A library to simplify usage of the Newrelic APM Java API from Clojure programs.
</small>
<br clear="all" /><br />

---

#### Creating web transactions

Use `wrap-transactions` to make sure that every web request creates a Newrelic transaction including basic information
about the request. This middleware should be placed on the absolute exterior of your application.

```clojure

```

#### Naming web transactions

Use `wrap-transactions-naming` to name transactions based on matching route data. This middleware should be placed just
after routing has occurred in your application. If data from Compojure or Reitit is present in the request map it will
use the route "template" as the name of the transaction instead of the literal uri.

```clojure

```

#### Injecting Client Side Real User Monitoring (RUM)

Optionally use the `wrap-rum-injection` middleware to add script tags to all html page responses. Injection is performed
by [injecting-streams](https://github.com/RutledgePaulV/injecting-streams) and takes place as bytes are written to the
response stream. The performance overhead is extremely low (~400ns).

```clojure

```

#### Creating function traces

```clojure

```

#### Reporting errors

```clojure

```


---

### License

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).