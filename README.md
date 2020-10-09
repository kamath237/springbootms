![](RackMultipart20201009-4-pf7et_html_963952ba62760d06.jpg)

![](RackMultipart20201009-4-pf7et_html_9002ef0552d0d726.gif)

#


![](RackMultipart20201009-4-pf7et_html_dcb0da31789ef842.gif)

# **MICROSERVICES API Proof Of Concept.**

Document History

| Author | Version | Date | Description |
| --- | --- | --- | --- |
| Mahesh | 0.1 | 5-Oct-20 | PoC draft |
|
 |
 |
 |
 |
|
 |
 |
 |
 |
|
 |
 |
 |
 |

**Table of Contents**

[Proposed Architecture 3](#_Toc52810225)

1.
#

1.
# Proposed Architecture

![](RackMultipart20201009-4-pf7et_html_997085919841b176.gif)

Detailed Diagram - TBD

1.
# Technology Components

  1.
## Netflix Zuul

Zuul is the front facing (edge service) gateway component for all requests from users and integrated systems. Zuul helps in achieving dynamic routing, resiliency and security.

    1.
### Proxy Server

      1.
#### Provisioning

The Netflix Eureka Server can be provisioned through a Springboot Application as follows:

| @SpringBootApplication
@EnableZuulProxy
**public class** MovieCatalogServiceApplication {

**public static void** main(String[] args) {
 SpringApplication._run_(MovieCatalogServiceApplication. **class** , args);
 }
 |
| --- |

      1.
#### Application Properties

| **eureka** :
**client** :
**fetchRegistry** : true
**registerWithEureka** : false
**registryFetchIntervalSeconds** : 1
**serviceUrl** :
**defaultZone** : ${EUREKA\_URI:http://localhost:8090/eureka}
**instance** :
**leaseExpirationDurationInSeconds** : 2
**leaseRenewalIntervalInSeconds** : 1
**server** :
**port** : 8089
**spring** :
**application** :
**name** : zuul-service

 _# Disable Hystrix timeout globally (for all services)_ **hystrix.command.default.execution.timeout.enabled** : false

**zuul** :
**ignoredServices** : **&quot;\*&quot;**
**movieinfo** :
**path** : /movies/\*\*
**sensitiveHeaders** :
_# serviceId as registed with Eureka. Enabled and used when ribbon.eureka.enabled is true._
**serviceId** : MOVIE-INFO-SERVICE
 _# stripPrefix set to true if context path is set to /_
**stripPrefix** : false
**ratinginfo** :
**path** : /ratingsdata/\*\*
**sensitiveHeaders** :
**serviceId** : RATINGS-DATA-SERVICE
**stripPrefix** : false
**debug** :
**requests** : true
**logging** :
**level** :
**org.springframework.web.filter.CommonsRequestLoggingFilter** : DEBUG
 |
| --- |

    1.
### Load Balancing

See below end point api/users/{userId} end point makes call to another service **/ratingsdata/users/** end point via the 8089 Zuul proxy host:port.

Zuul does the load balancing and you could see in the result of the two iterations invoked served from different instances of the same service.

| @RequestMapping( **&quot;/api/users/{userId}&quot;** )
**public** User getUserRating(@PathVariable( **&quot;userId&quot;** ) String userId, @RequestHeader( **&quot;authorization&quot;** ) String authzToken) {

 User user = **new** User ();
 UserRating userRating = **webClientBuilder**.build()
 .get()
 .uri( **&quot;http://localhost:8089/ratingsdata/users/&quot;** +userId)
 .header( **&quot;Authorization&quot;** ,authzToken)
 .retrieve()
 .bodyToMono(UserRating. **class** )
 .block(); |
| --- |

![](RackMultipart20201009-4-pf7et_html_e13467a0884b133c.png)

Upon stopping one of the server, the gateway gets the two iteration results from the only available instance.

![](RackMultipart20201009-4-pf7et_html_99c9321b080c9011.png)

    1.
### Dynamic Routing

Zuul achieves dynamic routing with the help of Eureka registry fetch.

Refer to the application properties

See the routes define the Eureka service ids and not the direct service url endpoints.

This helps Zuul to perform dynamic routing.

| **zuul** :
**ignoredServices** : **&quot;\*&quot;**
**routes** :
**cataloginfo** :
**path** : /catalog/\*\*
**sensitiveHeaders** :
**serviceId** : MOVIE-CATALOG-SERVICE
**stripPrefix** : false |
| --- |

**Note** : this edge service does not register itself with Eureka (registerWithEureka) because we are assuming that the individual underlying services would not need to invoke the outer facing edge service.

    1.
### Service Orchestration and Result Aggregation

There is no specific Zuul feature used to achieve Request splitting and Result aggregation.

| @RequestMapping( **&quot;/**** api/users/{userId ****}&quot;** )
**public** User getUserRating(@PathVariable( **&quot;userId&quot;** ) String userId, @RequestHeader( **&quot;authorization&quot;** ) String authzToken) {

 User user = **new** User ();
 UserRating userRating = **webClientBuilder**.build()
 .get()
 .uri( **&quot;**** http://localhost:8089/ratingsdata/users ****/&quot;** +userId)
 .header( **&quot;Authorization&quot;** ,authzToken)
 .retrieve()
 .bodyToMono(UserRating. **class** )
 .block();
user.setUserRating(userRating);
 List\&lt;Movie\&gt; movies = userRating.getUserRating().stream().map(userrating -\&gt; {
Movie movie = **webClientBuilder**.build()
 .get()
 .uri( **&quot;**** http://localhost:8089/movies ****/&quot;** + userrating.getMovieId())
 .header( **&quot;Authorization&quot;** ,authzToken)
 .retrieve()
 .bodyToMono(Movie. **class** )
 .block();

**return** movie;
 }).collect(Collectors._toList_());
user.setMovie(movies);

**return** user;
 } ![](RackMultipart20201009-4-pf7et_html_7fb2540522e58099.png)
 |
| --- |

    1.
### Authentication

There are two options for authentication config

1. Zuul redirect to Authorization server for authentication – zuul contacts auth server

2. First, consumers of services authenticate with OAM token end-point separately to acquire token and then embed this token in subsequent requests through Zuul gateway.

      1.
#### External Authentication Internal Validation

First, consumers of services authenticate with OAM token end-point separately to acquire token and then embed this token in subsequent requests through Zuul gateway.

        1.
##### Token Validation

Zuul&#39;s pre Filter does JWT Token validation implementation.

When we invoke an end-point registered as a Zuul route, it takes us through the pre-filter where the JWT token signature validation and token validity happens.

|
@Component
**public class** JWTValidationPreFilter **extends** ZuulFilter {@Override
**public** String filterType() {
**return**  **&quot;pre&quot;** ;
 }

@Override
**public int** filterOrder() {
**return** 0;
 }

@Override
**public boolean** shouldFilter() {
**return true** ;
 }@Override
**public** Object run() **throws** ZuulException {//JWT Token signature validation//JWT Token validation //JWT Claims extraction}
 |
| --- |

        1.
##### Fault handling

Below is not actual fault handling… TBD

![](RackMultipart20201009-4-pf7et_html_918ed89803b9ce90.png)

      1.
#### Internal Authentication

        1.
##### Gateway Config

In the below, we block all resources that aren&#39;t authenticated

| @Configuration
 @EnableResourceServer
**public class** GatewayConfiguration **extends** ResourceServerConfigurerAdapter {
@Override
**public void** configure( **final** HttpSecurity http) **throws** Exception {
 http.authorizeRequests()
 .antMatchers( **&quot;/\*\*&quot;** )
 .authenticated();
 }
 }
 |
| --- |

An unauthenticated request

![](RackMultipart20201009-4-pf7et_html_3743dab1d5f95833.png)

        1.
##### Pom Configuration

| \&lt; **dependency** \&gt;
\&lt; **groupId** \&gt;org.springframework.security.oauth\&lt;/ **groupId** \&gt;
\&lt; **artifactId** \&gt;spring-security-oauth2\&lt;/ **artifactId** \&gt;
\&lt; **version** \&gt;2.3.3.RELEASE\&lt;/ **version** \&gt;
\&lt;/ **dependency** \&gt;
\&lt; **dependency** \&gt;
\&lt; **groupId** \&gt;org.springframework.security\&lt;/ **groupId** \&gt;
\&lt; **artifactId** \&gt;spring-security-jwt\&lt;/ **artifactId** \&gt;
\&lt; **version** \&gt;1.0.9.RELEASE\&lt;/ **version** \&gt;
\&lt;/ **dependency** \&gt;
 |
| --- |

  1.
## Netflix Eureka Service Registry

Eureka is a convenient way to abstract the discovery of remote servers so that you do not have to hard code their URLs in clients.

A service registry is useful because it decouples service providers from consumers and helps in availability monitoring in real time.

    1.
### Eureka Server

![](RackMultipart20201009-4-pf7et_html_714d46c0e433f1cd.png)

      1.
#### Provisioning

A Springboot application can be provisioned as Eureka Server as below

| @SpringBootApplication
 @EnableEurekaServer
**public class** DiscoveryServerApplication { |
| --- |

      1.
#### Application properties

The properties mentioned below can be tuned further based on more studies.

Self preservation mode is turned off so that Eureka server becomes aggressive in registering the node statuses.

| **server.port** = **8090**
**eureka.client.register-with-eureka** = **false**
**eureka.client.fetch-registry** = **false**
**eureka.client.serviceUrl.defaultZone** = **http://localhost:${server.port}**

_#eureka.server.enable-self-preservation: Configuration for disabling self-preservation – the default value is true_
**eureka.server.enableSelfPreservation** = **false**

_#eureka.server.expected-client-renewal-interval-seconds: The server expects client heartbeats at an interval configured with this property – the default value is 30
 #eureka.server.expected-client-renewal-interval-seconds=15

 #eureka.instance.lease-expiration-duration-in-seconds: Indicates the time in seconds that the Eureka server waits since it received the last heartbeat from a client before it can remove that client from its registry – the default value is 90
 #eureka.instance.lease-expiration-duration-in-seconds=17

 #eureka.server.eviction-interval-timer-in-ms: This property tells the Eureka server to run a job at this frequency to evict the expired clients – the default value is 60 seconds_ **eureka.server.eviction-interval-timer-in-ms** = **3000**

_#eureka.server.renewal-percent-threshold: Based on this property, the server calculates the expected heartbeats per minute from all the registered clients – the default value is 0.85
 #eureka.server.renewal-threshold-update-interval-ms: This property tells the Eureka server to run a job at this frequency to calculate the expected heartbeats from all the registered clients at this minute – the default value is 15 minutes

 #Eureka Server - eureka.server.responseCacheUpdateInvervalMs
 #
 #30 seconds. Eureka server&#39;s APIs have its own cache for response. The default is quite big period. You can decrease this value._ **eureka.server.responseCacheUpdateInvervalMs** = **2000**
 |
| --- |

    1.
### Eureka Client

      1.
#### Provisioning

A springboot application can register itself with the Discovery service registry (eureka server) as follows:

| @SpringBootApplication
 @EnableDiscoveryClient
**public class** MovieInfoServiceApplication {

**public static void** main(String[] args) {
 SpringApplication._run_(MovieInfoServiceApplication. **class** , args);
 }
 |
| --- |

      1.
#### Application Properties

| **server.port** = **8092**
**spring.application.name** = **movie-info-service**
_# eureka.instance.lease-renewal-interval-in-seconds indicates the interval of heartbeats that the client sends to the server. The default value is 30 seconds which means that the client will send one heartbeat every 30 seconds._
**eureka.instance.leaseRenewalIntervalInSeconds** = **1**
_#Eureka Client (API Provider) eureka.instance.leaseExpirationDurationInSeconds
 #
 #90 seconds. Each instance that registers to Eureka server can set the expiration duration for itself.
 # Eureka server expires instance if it doesn&#39;t receive any heartbeat from Eureka client during this period.
 # As a default, each Eureka client sends the heartbeat every 30s._ **eureka.instance.leaseExpirationDurationInSeconds** = **2**

**eureka.client.serviceUrl.defaultZone** = **http://localhost:8090/eureka/**
 |
| --- |

  1.
## Springboot REST micro services

We need to write REST services for all that LDAP CRUD operations 

These services have to be registered as clients to Eureka Discovery service

Sample service below:

    1.
### Incoming request validation

If in the incoming request the authorization header isn&#39;t found, the method returns a &quot;400 Bad Request&quot; error.

However, this needs more refinement to restrict the requestor is only Zuul proxy

| @RequestMapping( **&quot;/ratingsdata/users/{userId}&quot;** )
**public** UserRating getUserRating(@PathVariable( **&quot;userId&quot;** ) String userId,@RequestHeader( **&quot;authorization&quot;** ) String authzToken) { |
| --- |

| ![](RackMultipart20201009-4-pf7et_html_218b4e52060cc94d.png) | ![](RackMultipart20201009-4-pf7et_html_befbdd70c38d7c22.png) |
| --- | --- |

    1.
### Incoming Request logging

  1.
## External REST services


These services have to be registered as clients to Eureka Discovery service - also explore 'sidecar'

1.
# Spring Boot Services Health monitoring and Metrics
2.
# Hardware Requirements

Based on Performance assessment results, we could estimate CPU and RAM requirements

1.
# Software Requirements

Need to assess commercial usage aspects of Netflix Zuul and Netflix Eureka and Springboot Embedded Tomcat

1.
# Performance Assessment Results

TBD – Performance testing using Jmeter or something else?
