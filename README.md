## URL Shortener

### Running

Requires [sbt](http://www.scala-sbt.org/0.13/docs/Setup.html).

```bash
sbt run
```

The server will run at `http://localhost:8080`

### Endpoints

#### POST /shorten

Expects a string containing a well-formed URL, returns the shortened path.

Example:

```bash
curl http://localhost:8080/shorten --data 'http://google.com'
```

#### POST /getUrl

Expects the shortened path (eg "jX3L2wc") and returns the long URL.

Example:

```bash
curl http://localhost:8080/getUrl --data 'jX3L2wc'
```

#### POST /getClicks

Expects the shortened path (eg "jX3L2wc") and returns the number of times the link has been used.

Example:

```bash
curl http://localhost:8080/getClicks --data 'jX3L2wc'
```

#### GET /[shortened path]

Redirects to the URL given the shortened path.

Example (really you should use your browser):

```bash
curl -L http://localhost:8080/jX3L2wc
```
