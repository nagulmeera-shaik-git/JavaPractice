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