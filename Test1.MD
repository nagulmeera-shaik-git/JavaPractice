ExecutorService

#Imagine your Spring Boot application receives 1,000 requests.Every request needs to perform some background processing?

##"Creating a new thread for every task is not scalable because threads have memory and scheduling overhead. If the application receives a large number of tasks, creating thousands of threads can cause excessive memory consumption, context switching and CPU overhead, and can eventually exhaust system resources.

I would use a thread pool through ExecutorService or ThreadPoolExecutor. The pool maintains a controlled number of worker threads and processes submitted tasks from a queue. This allows us to control concurrency, reuse threads and protect the application from creating an unbounded number of threads.

The pool size should be selected based on whether the workload is CPU-bound or I/O-bound, along with CPU cores, expected latency and throughput requirements."

#**How do you decide thread-pool size?**

Say:
##**It depends on whether the workload is CPU-bound or I/O-bound. For CPU-bound tasks, I would generally start around the number of available CPU cores because excessive threads can cause context switching.
For I/O-bound tasks, I can use a larger pool because threads spend significant time waiting for external resources. However, I wouldn't choose the size arbitrarily; I would consider I/O latency, throughput, CPU utilization, memory, downstream limits such as database connection pools, and validate the configuration through load testing.**