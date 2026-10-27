Spring Boot
---------------------
IOC:

DI:


1) management.endpoints.web.exposure.enabled=*
2) management.endpoints.web.exposre.exclude=beans
3) management.endpoints.shutdown.enabled=true
4)

spring bean life cycle

Component Scan
      ↓
Bean Definition
      ↓
Bean Instantiation
      ↓
Dependency Injection
      ↓
BeanPostProcessor
      ↓
@PostConstruct
      ↓
Bean Ready
      ↓
Application Running
      ↓
@PreDestroy

-----------------------------------------------
Why do you prefer constructor injection?
I prefer constructor injection because it makes the class's dependencies explicit and ensures mandatory dependencies are available when the object is created. It also allows dependencies to be declared as final, improves immutability, and makes unit testing easier because I can instantiate the class directly without relying on the Spring container
-----------------------------------------------

What is the difference between @Component, @Service, and @Repository?
@Component, @Service, and @Repository are stereotype annotations that allow Spring to discover and manage classes as beans through component scanning. @Component is generic, @Service is intended for the business/service layer, and @Repository is intended for the persistence layer and also enables Spring's data-access exception translation.
------------------------------------------------

What is a Spring Bean?

A Spring Bean is an object whose complete lifecycle is managed by the Spring IoC container. Spring is responsible for creating the object, resolving and injecting its dependencies, applying BeanPostProcessors such as transaction and AOP proxies, executing lifecycle callbacks like @PostConstruct and @PreDestroy, and making the bean available through the ApplicationContext for the rest of the application.

A Spring Bean is an object whose creation, configuration, dependency injection, and lifecycle are managed by the Spring IoC container. A normal object created using new is managed directly by the application code and is outside Spring's bean lifecycle unless it is registered with the container.

Spring's default bean scope is singleton, so normally one instance of the bean is created per ApplicationContext and shared wherever it is injected. Spring also supports other scopes such as prototype, request, and session depending on the application's requirement.
If OrderService is a singleton and 100 requests are using the same instance concurrently, is that a problem?
Think about whether the class has instance variables/state.
One object is shared by many threads.

@Service
public class OrderService {

    private int count = 0;
}
has one count variable shared by all requests using that OrderService instance.
If 100 requests come at the same time, multiple threads may access that same object.
That can cause problems if the bean contains mutable shared state.
public void processOrder() {
    count++;
}

Multiple threads doing count++ can cause a race condition.


Singleton beans are shared, so they should generally be stateless and thread-safe.
Avoid mutable request-specific data like:
private String currentUser;
private int count;
private Order currentOrder;

**A singleton Spring bean is shared across multiple threads, so we need to be careful with mutable instance state. Stateless singleton services are generally safe because method-local variables are thread-specific. If a singleton maintains shared mutable state, we need proper synchronization or thread-safe mechanisms to avoid race conditions**
----------------------------
@PostConstruct is called by Spring after the bean has been instantiated and its dependencies have been injected, but before the bean is made available for normal use. It's commonly used for initialization logic.
Create → Inject → PostConstruct → Ready
----------------------------
@PreDestroy is invoked by the Spring container before a managed bean is destroyed. It is typically used for cleanup activities such as releasing resources or stopping background processes.
-----------------------------
What is Auto-Configuration?

Spring Boot looks at:

What dependencies are present in the classpath
What beans/configuration already exist
Application properties/configuration
Certain conditions

Based on those, Spring Boot automatically provides appropriate configuration and beans.

Spring Boot auto-configuration automatically configures the application based on the dependencies available on the classpath, existing beans, application properties, and conditional configuration. It reduces the amount of manual configuration required. For example, adding a web starter allows Spring Boot to automatically configure much of the web application infrastructure.
----------------------------------------------------------------

In one enterprise application, we had a PDF generation module using Apache PDFBox along with several custom fonts and templates. Initializing that module required loading dozens of font files into memory, which increased application startup time by several seconds. Since PDF generation was used only by administrators, we annotated that service with @Lazy. The application started faster, and the PDF module was initialized only when the first administrator requested a report.

-----------------------------------------------------------------

I use @Lazy selectively for expensive or rarely used components, such as reporting modules, legacy integrations, or large initialization tasks that don't need to be available immediately after startup. I avoid using it for core business services or security infrastructure because eager initialization helps detect configuration problems early and ensures the application is fully ready before it starts serving requests

-----------------------------------------------------------------
GET /orders/101

Can you explain the high-level flow of what happens inside Spring Boot from the HTTP request reaching the application until getOrder(101) is executed?

The HTTP request first reaches the embedded web server and then Spring MVC's DispatcherServlet. The DispatcherServlet uses the request URL and HTTP method to find the appropriate controller handler method. For GET /orders/101, it matches /orders and /{id}, extracts the path variable as 101, and invokes the controller method. The controller then calls the service layer, which typically interacts with the repository or database, and the result is converted into the HTTP response.

------------------------------------------------------------
"PUT is generally used to replace the complete resource representation, 
while PATCH is used for partial updates. 
PUT is intended to be idempotent, 
whereas PATCH may or may not be idempotent depending on the operation."
---------------------------------------------------------------
You have:
POST /orders

The request is valid, but an order with the same order ID already exists, and creating it again would violate a unique constraint.

I would return 409 Conflict when a valid request cannot be completed because it conflicts with the current state of the resource, such as creating a duplicate order where the order ID has a unique constraint
-------------------------------------------------------------------
@Transactional
public void placeOrder() {
    saveOrder();
    processPayment();
    updateInventory();
}

saveOrder() succeeds, but processPayment() throws a RuntimeException.
What normally happens to the database changes made inside this transaction?
Spring's @Transactional rolls back the transaction when an unchecked exception such as RuntimeException is thrown.
By default, if a RuntimeException or Error occurs within a @Transactional method, Spring marks the transaction for rollback. So the database changes made earlier in that transaction are rolled back when the transaction completes.

What happens if processPayment() throws a checked exception, for example:
by default, checked exceptions do not trigger rollback.
----------------------------------------------------------------
@Transactional works through Spring's proxy mechanism. So if you have:

public void methodA() {
    methodB();
}

@Transactional
public void methodB() {
    ...
}
and methodA() and methodB() are called within the same class, the transactional proxy may be bypassed due to self-invocation.
Outside call → Proxy → Transaction
Same-class call → bypass Proxy → No transactional interception
---------------------------------------------------------------
Self-invocation

Same class → bypasses Spring proxy → @Transactional may not apply.

2️⃣ Local transaction

@Transactional controls the transaction managed by that application/resource; it doesn't automatically span independent microservices.

3️⃣ Distributed transaction

For multiple microservices, use distributed transaction patterns such as Saga and compensating actions rather than assuming one local transaction will roll everything back.
-------------------------------------------------------------------
**How do you handle distributed transactions in microservices?**
In a microservices architecture, I generally avoid trying to maintain one large ACID transaction across multiple services. 
Each service maintains its own local transaction and database. For business workflows spanning multiple services, I would use the Saga pattern, either choreography or orchestration, with compensating actions when a later step fails. 
For reliable event publishing, I would consider the Outbox Pattern. 
2PhaseCommit is another option, but it introduces coordination and availability/performance overhead, 
so Saga is often more suitable for microservices.
------------------------------------------------------------------
Order DB
   ↓
Save Order ✅

Then publish event ❌
   ↓
Application crashes 💥

Outbox solves this
Instead of doing:
Save Order
Publish Event

we do both DB operations in one local transaction:
┌──────────── Order DB ────────────┐
│                                  │
│  Orders table                    │
│  Order 101                       │
│                                  │
│  Outbox table                    │
│  OrderCreated event              │
│                                  │
└──────────────────────────────────┘
             ↑
       ONE transaction
If the transaction succeeds:
Order saved ✅
Event stored in Outbox ✅

Then a separate publisher reads the Outbox:
Outbox
   ↓
Publisher
   ↓
Message Broker
   ↓
Payment Service

If publishing temporarily fails, the event remains in the Outbox and can be retried.
🧠 Remember this forever
“Business data + event record = one local transaction; publish the event afterward.

What is the main purpose of the Outbox Pattern?
The Outbox Pattern solves the dual-write problem between a database and a message broker. We store the business change and an event record in an Outbox table within the same local database transaction. A separate publisher then reads the Outbox and publishes the event to the broker, with retries if publishing fails.
------------------------------------------------------------------
Saga can be implemented in two common ways.
In Saga orchestration, a central orchestrator coordinates the workflow and invokes each participating service, including compensating actions when required. In choreography, there is no central coordinator; services communicate through events and react to events published by other services.
-----------------------------------------------------------------
**Circuit Breaker**
What is a Circuit Breaker?
A circuit breaker is a resilience pattern used to prevent repeated calls to a failing or slow downstream service. 
When failures cross a configured threshold, the circuit opens and subsequent calls fail fast instead of continuously hitting the unhealthy service. 
After a wait period, it moves to half-open and allows limited test requests. If the dependency recovers, the circuit closes; otherwise, it opens again.

It protects your service from continuously depending on an unhealthy downstream service.
**Retry handles temporary failures; Circuit Breaker handles repeated failures.**
-------------------------------------------------------------------
**Bulkhead Pattern**
Bulkhead = isolate resources so one failing dependency doesn't take down everything.

The resources can be isolated using things such as:
Separate thread pools
Connection pools
Concurrency limits
Semaphores

Now connect the four resilience patterns


**TIMEOUT
   ↓
"Don't wait forever"

RETRY
   ↓
"Try temporary failures again"

CIRCUIT BREAKER
   ↓
"Stop calling a repeatedly failing service"

BULKHEAD
   ↓
"Don't let one dependency consume everything"**


                 Downstream Service
                        ↓
                    TIMEOUT
                        ↓
              Temporary failure?
                   ↙          ↘
                 YES           NO
                  ↓             ↓
               RETRY       Handle failure
                  ↓
             Still failing?
                  ↓
          CIRCUIT BREAKER
                  ↓
             OPEN 🔴
                  ↓
             FAIL FAST
                  ↓
             FALLBACK
---------------------------------------------------------------------
**How do you prevent duplicate payments during retries?**
I would use an idempotency key for the payment request. The payment service stores the key along with the processing result. If the client retries with the same key, the service recognizes that the operation was already processed and returns the existing result instead of processing the payment again. This is especially important because a timeout doesn't necessarily mean the original operation failed.             
---------------------------------------------------------------------
**Spring Boot Microservices**
You have:

Order Service
     |
     ↓
Payment Service
     |
     ↓
Inventory Service

If Order Service directly accesses Payment Service's database, what is the main architectural problem?
Each microservice should own its data and database. If another service directly accesses that database, it creates tight coupling at the database and schema level. Changes in the database can impact multiple services and reduce independent deployment, scalability, and maintainability. Other services should communicate through well-defined APIs or events.
-------------------------------------------------------------------------
You have 10 instances of Payment Service running:

Payment-1
Payment-2
Payment-3
...
Payment-10

Order Service needs to call Payment Service.
How does Order Service know which Payment Service instance to call?
In a microservices environment,** we typically use service discovery and load balancing so that the caller doesn't need to know the IP address of a particular instanc**e. Service discovery provides the available instances, and the load balancer routes requests to a suitable healthy instance
--------------------------------------------------------------------------
**API Gateway**
An API Gateway acts as the entry point for external clients in a microservices architecture. It routes requests to appropriate services and can also handle cross-cutting concerns such as 
authentication, authorization, rate limiting, logging, and sometimes load balancing. 
It also prevents clients from becoming tightly coupled to the internal service topology.

--------------------------------------------------------------------------
Order Service calls Payment Service:

Order Service → Payment Service
                     ↓
                 Payment SUCCESS
                     ↓
              response is lost
                     ↓
Order Service gets TIMEOUT ❌

Order Service thinks:

"Payment failed, let's try again."

What problem can happen? payment happens twice
A timeout doesn't guarantee that the downstream operation failed. The payment may have succeeded but the response may have been lost. If the caller retries without protection, it can result in duplicate payment. **We should use an idempotency key so that repeated requests for the same operation return the existing result instead of executing the operation again.**
---------------------------------------------------------------------------------
here's a potential problem here:

Order DB transaction
       +
Publish event

What if Order Service saves the order successfully but crashes before publishing OrderCreated?

Order saved ✅
       ↓
Application crashes 💥
       ↓
Event NOT published ❌

Now Payment Service never knows about the order.

This is where the Outbox Pattern becomes useful.
The Outbox Pattern solves the dual-write problem between a database and a message broker. We store the business change and an event record in an Outbox table within the same local database transaction. A separate publisher then reads the Outbox and publishes the event to the broker, with retries if publishing fails.

----------------------------------------------------------------------------------------
Spring has had different HTTP client approaches:

RestTemplate → traditional, synchronous/blocking
WebClient    → modern, supports non-blocking/reactive

WebClient is Spring's modern HTTP client and is designed to support non-blocking, reactive communication. It can also be used in traditional applications, but if we call block(), we lose the non-blocking benefit.
---------------------------------------------------------

Suppose Order Service calls Payment Service using WebClient.

Payment Service takes 30 seconds to respond.

What should we configure so Order Service doesn't wait indefinitely?
For downstream REST calls, I would configure connection and response/read timeouts so that the caller doesn't wait indefinitely. The timeout should be based on the business SLA and downstream behavior. For further resilience, I would combine it with circuit breaking, limited retries where appropriate, and proper fallback handling
-------------------------------------------------------------
Client
  |
  | Trace-ID: ABC123
  ↓
API Gateway
  |
  | ABC123
  ↓
Order Service
  |
  | ABC123
  ↓
Payment Service
  |
  | ABC123
  ↓
Inventory Service

Trace ID → identifies the entire request journey

Span ID → identifies one operation within that journey
--------------------------------------------------------------
Rate limiting controls how many requests a client can make within a defined time or quota. It protects downstream services from excessive traffic, helps prevent abuse, and provides predictable resource usage. When the limit is exceeded, the gateway can return HTTP 429.

--------------------------------------------------------
Don't put secrets such as database passwords casually into a config repository. In production, secrets are generally handled through a secret-management solution such as AWS Secrets Manager, Parameter Store, Vault, Kubernetes Secrets, etc., depending on the environment
-----------------------------------------------------
