1. Your Spring Boot application suddenly becomes slow in production. CPU and memory look normal. What would you investigate?
2. An API works perfectly in Postman, but sometimes returns 500 Internal Server Error in production. How would you debug it?
3. Your application is creating too many database connections and eventually throws connection pool exhausted errors. What could be wrong?
4. A Hibernate query is taking several seconds even though the table has only a few thousand records. How would you investigate and optimize it?
5. Your application works correctly with one instance, but behaves inconsistently when multiple instances are running. What could be the reason?
6. Users are receiving stale data even though the database contains the latest information. Where would you look?
7. An API suddenly starts throwing OutOfMemoryError after running continuously for several hours. How would you identify the root cause?
8. A Spring Boot application starts successfully, but one specific API fails with a LazyInitializationException. Why does this happen and how would you fix it?
9. One external REST API becomes very slow, causing your entire Java application to become slow. What design mechanisms would you implement?
10. Everything works in the developer environment, but the application fails immediately after deployment to production. What would you verify first?

1. How does Spring Boot Auto-Configuration work internally, and how can you customize it?
2. Explain the Spring Bean Lifecycle and the role of BeanPostProcessor.
3. How does Spring handle circular dependencies between beans?
4. Why does @Transactional fail during self-invocation?
5. Explain transaction propagation and isolation levels with real-world scenarios.
6. What happens internally when an HTTP request reaches a Spring Boot REST API?
7. How do @ControllerAdvice and @ExceptionHandler work internally?
8. How would you implement API versioning while maintaining backward compatibility?
9. Explain the Spring Security filter chain and JWT-based authentication.
10. How would you troubleshoot high response times in a production Spring Boot application?
11. How do @Cacheable, @CachePut, and @CacheEvict differ?
12. How would you ensure idempotency and prevent duplicate requests in REST APIs?
13. How would you implement retries, timeouts, and circuit breakers in microservices?
14. How do you manage externalized configuration and secrets across multiple environments?
15. How would you implement distributed tracing, centralized logging, and health monitoring in a microservices architecture?
    
ROUND 1: CORE JAVA, DSA & PROBLEM SOLVING
• How does HashMap work internally? Explain collisions and resizing.
• Find the longest substring without repeating characters in O(n) time.
• Detect a cycle in a linked list without using extra space.
• What happens when multiple threads modify the same shared object?
• What happens if you override equals() but not hashCode()?

ROUND 2: SPRING BOOT & MICROSERVICES
• What happens internally when a method annotated with @Transactional is called?
• Why can @Transactional fail during self-invocation?
• How would you make a REST API idempotent to prevent duplicate transactions?
• How would you handle failures in downstream microservices?
• How would you troubleshoot a slow API in production?

ROUND 3: SYSTEM DESIGN & REAL-WORLD SCENARIOS
• Design a highly available payment system that prevents duplicate transactions.
• How would you maintain data consistency across microservices without distributed transactions?
• How would you troubleshoot memory leaks and thread pool exhaustion?
• How would you design a backend system to handle thousands of concurrent requests?
• When would you choose Kafka over synchronous REST communication, and why?

ROUND 4: HR & MANAGERIAL DISCUSSION
• Tell me about a challenging production issue you solved.
• Why do you want to join JP Morgan?
• How do you handle disagreements during technical discussions?
• What are your long-term career goals?

