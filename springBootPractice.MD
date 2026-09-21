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


