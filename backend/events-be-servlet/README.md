
# Asynchronous RESTful API with Java 8 Lambda and JAX-RS and Apache Cassandra

design notes
- leverage asynchronous and non-blocking APIs in all layers where applicable
- use NoSQL storage engine
- apply Java 8 lambdas
- use Servlet 3.1 API + JAX-RS 2.0
- minimize other dependencies (don't e.g. CI etc. frameworks etc.)

technologies and APIs
- Servlet 3.1 API
- JAX-RS 2.0 API
- Jersey 2
- Apache Cassandra 2
- tested on Tomcat 8 / Jetty 9
