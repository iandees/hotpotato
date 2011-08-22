hotpotato
=========

**hotpotato** - or hptt, from the common misspelling of http - is a Java high performance and throughput oriented HTTP client library, with support for HTTP 1.1 pipelining.
It was developed mostly towards server-side usage, where speed and low resource usage are the key factors, but can be used to build client applications as well.

Built on top of Netty and designed for high concurrency scenarios where multiple threads can use the same instance of a client without any worries for external or internal synchronization, **hotpotato** helps you reduce initialization and/or preparation times and resource squandering.
Among many small optimizations, connections are reused whenever possible, which results in a severe reduction of total request execution times by cutting connection establishment overhead.

Project page: [http://hotpotato.biasedbit.com](http://hotpotato.biasedbit.com)

Dependencies
------------

* JDK 1.6
* [SLF4J 1.6](http://www.slf4j.org/download.html)
* [Netty 3.2](http://jboss.org/netty/downloads.html)

License
-------

**hotpotato** is licensed under the [Apache License version 2.0](http://www.apache.org/licenses/LICENSE-2.0) as published by the Apache Software Foundation.

Quick & Dirty examples
----------------------

### Synchronous mode

This example contains all the steps to execute a request, from creation to cleanup.
This is the synchronous mode, which means that the calling thread will block until the request completes.

    // Create & initialise the client
    HttpClient client = new DefaultHttpClient();
    client.init();

    // Setup the request
    HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_0,
                                                 HttpMethod.GET, "/");

    // Execute the request, turning the result into a String
    HttpRequestFuture future = client.execute("hotpotato.biasedbit.com", 80, request,
                                              new BodyAsStringProcessor());
    future.awaitUninterruptibly();
    // Print some details about the request
    System.out.println(future);
        
    // If response was >= 200 and <= 299, print the body
    if (future.isSuccessfulResponse()) {
        System.out.println(future.getProcessedResult());
    }

    // Cleanup
    client.terminate();

### Asynchronous mode

In asynchronous mode, an event listener is attached to the object returned by the http client when a request execution is submitted. Attaching this listener allows the programmer to define some computation to occur when the request finishes.

Only the relevant parts are shown here.

    // Execute the request
    HttpRequestFuture<String> future = client.execute("hotpotato.biasedbit.com", 80, request,
                                                      new BodyAsStringProcessor());
    future.addListener(new HttpRequestFutureListener<String>() {
        @Override
        public void operationComplete(HttpRequestFuture future) throws Exception {
            System.out.println(future);
            if (future.isSuccessfulResponse()) {
                System.out.println(future.getProcessedResult());
            }
            client.terminate();
        }
    });

Note that you should **never** perform non-CPU bound operations in the listeners.

Using the **HttpClient API** directly you may need to add extra headers manually.
You'll also need to handle redirect codes and authentication manually.
This is the API to use when you want absolute control over each and every request.

    request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                     "/webhp?hl=pt-PT&tab=iw");
    request.addHeader(HttpHeaders.Names.HOST, "www.google.pt");
    future = client.execute("www.google.pt", 80, request, new BodyAsStringProcessor());

Using the **HttpSession API**, everything is simpler and the library handles redirects automatically.
Proxy support is also included, as well as Digest/Basic auth and optionally Cookie storage!
This is the API to use when you just want to get things done and avoid repetitive task hassles.

    DefaultHttpSession session = new DefaultHttpSession(httpClient);
    // Setup a proxy
    session.setProxy("41.190.16.17", 8080);
    // And add cookie handling capabilities
    session.addHandler(new CookieStoringResponseHandler());

    session.execute("http://google.com", HttpVersion.HTTP_1_1, HttpMethod.GET,
                    new BodyAsStringProcessor());

The following is a comprehensive example of request to three distinct servers.

    final DefaultHttpClient client = new DefaultHttpClient();
    client.setRequestTimeoutInMillis(5000);
    client.init();

    final CountDownLatch latch = new CountDownLatch(3);

    HttpRequest request;
    HttpRequestFuture<String> future;

    request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
    request.addHeader(HttpHeaders.Names.HOST, "hotpotato.biasedbit.com");
    future = client.execute("hotpotato.biasedbit.com", 80, request, new BodyAsStringProcessor());
    future.addListener(new HttpRequestFutureListener<String>() {
      @Override
      public void operationComplete(HttpRequestFuture<String> future) throws Exception {
          System.out.println("\nHotpotato request: " + future);
          if (future.isSuccess()) {
              System.out.println(future.getResponse());
          } else {
              System.out.println(future.getResponse());
              future.getCause().printStackTrace();
          }
          if (future.isSuccessfulResponse()) {
              System.out.println(future.getProcessedResult());
          }
          latch.countDown();
      }
    });

    request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                   "http://www.google.pt/webhp?hl=pt-PT&tab=iw");
    request.addHeader(HttpHeaders.Names.HOST, "www.google.pt");
    future = client.execute("www.google.pt", 80, request, new BodyAsStringProcessor());
    future.addListener(new HttpRequestFutureListener<String>() {
      @Override
      public void operationComplete(HttpRequestFuture<String> future) throws Exception {
          System.out.println("\nGoogle request: " + future);
          if (future.isSuccess()) {
              System.out.println(future.getResponse());
          } else {
              System.out.println(future.getResponse());
              future.getCause().printStackTrace();
          }
          if (future.isSuccessfulResponse()) {
              System.out.println(future.getProcessedResult());
          }
          latch.countDown();
      }
    });

    request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                                   "http://twitter.com/");
    future = client.execute("twitter.com", 80, request, new BodyAsStringProcessor());
    request.addHeader(HttpHeaders.Names.HOST, "twitter.com");
    future.addListener(new HttpRequestFutureListener<String>() {
      @Override
      public void operationComplete(HttpRequestFuture<String> future) throws Exception {
          System.out.println("\nTwitter request: " + future);
          if (future.isSuccess()) {
              System.out.println(future.getResponse());
          } else {
              System.out.println(future.getResponse());
              future.getCause().printStackTrace();
          }
          if (future.isSuccessfulResponse()) {
              System.out.println(future.getProcessedResult());
          }
          latch.countDown();
      }
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    client.terminate();

### Integration with IoC containers

**hotpotato** was developed to be fully IoC compliant.

Here's a simple example of how to configure a client in [Spring](http://www.springsource.org/):

    <bean id="httpClient" class="com.biasedbit.hotpotato.client.DefaultHttpClient"
          init-method="init" destroy-method="terminate">
      <property ... />
    </bean>

Or using a client factory, in case you want multiple clients:

    <bean id="httpClientFactory" class="com.biasedbit.hotpotato.client.factory.DefaultHttpClientFactory">
      <property ... />
    </bean>

HttpClientFactories will configure each client produced exactly how they were configured - they have the same options as (or more than) the HttpClients they generate.
Instead of having some sort of Configuration object, you configure the factory and then call getClient() in it to obtain a pre-configured client.

You can also create a client for a component, based on a predefined factory:

    <bean id="someBean" class="com.biasedbit.SomeComponent">
      <property name="httpClient">
        <bean factory-bean="httpClientFactory" factory-method="getClient" />
      </property>
      <property ... />
    </bean>

Note that you can accomplish the same effect as the factory example by simply using an abstract definition of a HttpClient bean and then using Spring inheritance.