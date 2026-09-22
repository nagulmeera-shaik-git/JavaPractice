Spring Boot
---------------------
IOC:

DI:


1) management.endpoints.web.exposure.enabled=*
2) management.endpoints.web.exposre.exclude=beans
3) management.endpoints.shutdown.enabled=true
4)
-----------------------------------------------
Why do you prefer constructor injection?
I prefer constructor injection because it makes the class's dependencies explicit and ensures mandatory dependencies are available when the object is created. It also allows dependencies to be declared as final, improves immutability, and makes unit testing easier because I can instantiate the class directly without relying on the Spring container
-----------------------------------------------
What is the difference between @Component, @Service, and @Repository?
@Component, @Service, and @Repository are stereotype annotations that allow Spring to discover and manage classes as beans through component scanning. @Component is generic, @Service is intended for the business/service layer, and @Repository is intended for the persistence layer and also enables Spring's data-access exception translation.
------------------------------------------------
What is Auto-Configuration?

Spring Boot looks at:

What dependencies are present in the classpath
What beans/configuration already exist
Application properties/configuration
Certain conditions

Based on those, Spring Boot automatically provides appropriate configuration and beans.

Spring Boot auto-configuration automatically configures the application based on the dependencies available on the classpath, existing beans, application properties, and conditional configuration. It reduces the amount of manual configuration required. For example, adding a web starter allows Spring Boot to automatically configure much of the web application infrastructure.

-----------------------------------------------------------
GET /orders/101

Can you explain the high-level flow of what happens inside Spring Boot from the HTTP request reaching the application until getOrder(101) is executed?

The HTTP request first reaches the embedded web server and then Spring MVC's DispatcherServlet. The DispatcherServlet uses the request URL and HTTP method to find the appropriate controller handler method. For GET /orders/101, it matches /orders and /{id}, extracts the path variable as 101, and invokes the controller method. The controller then calls the service layer, which typically interacts with the repository or database, and the result is converted into the HTTP response.

------------------------------------------------------------
"PUT is generally used to replace the complete resource representation, 
while PATCH is used for partial updates. 
PUT is intended to be idempotent, 
whereas PATCH may or may not be idempotent depending on the operation."
---------------------------------------------------------------
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