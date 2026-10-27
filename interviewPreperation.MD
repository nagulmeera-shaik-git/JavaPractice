How does HashMap work internally? What changed in Java 8? 

HashMap stores key-value pairs using an array of buckets. When we call put(key, value), HashMap calculates the hash of the key using hashCode() and applies a hash-spreading operation. That hash is used to determine the bucket index.
If the bucket is empty, a new entry is inserted. If there is already an entry in the bucket, it means there is a collision, so HashMap compares the keys using hash information and equals(). If the keys are equal, the existing value is replaced. Otherwise, the new entry is added to that bucket.
In Java 8 and later, when a bucket becomes sufficiently large, HashMap can convert the linked-list structure into a red-black tree to improve lookup performance.
HashMap also maintains a load factor, which is 0.75 by default. When the number of entries crosses the threshold of capacity multiplied by the load factor, the table is resized, typically doubling its capacity, and entries are redistributed.
For get(key), HashMap calculates the hash again, identifies the bucket, and then compares the key with entries in that bucket using hash information and equals() to find the corresponding value.

in java8 changed linked list it replaced with red black tree for better search

-------------------------------
equals hashcode contract
equals = true  → hashCode MUST be same
hashCode same  → equals MAY be true or false
--------------------------------
What happens if we override equals() but don't override hashCode()?
If I override equals() without overriding hashCode(), I can violate the equals-hashCode contract. Two objects that are logically equal may produce different hash codes. 
When those objects are used as HashMap keys, put() may store the entry in one bucket while get() searches another bucket, causing the lookup to fail. Therefore, whenever we override equals(), we should override hashCode() consistently.
----------------------------------
Consider:
Map<Employee, String> map = new HashMap<>();

Employee e1 = new Employee(101, "John");
map.put(e1, "Developer");
e1.setName("Mike");

System.out.println(map.get(e1));

Assume both equals() and hashCode() use id and name.
What will happen and why?
Because name is part of both equals() and hashCode(), changing name after the object has been inserted changes its hash code. The entry was originally placed in a bucket based on the old hash code, but during get() HashMap calculates the new hash code and may search a different bucket. Therefore, HashMap may not be able to locate the existing entry, and get() can return null. This is why mutable fields used in equals() and hashCode() should generally not be changed while the object is being used as a HashMap key

So how can we get "Developer"?( since above SOP will give null since hashcode changed)
I would remove the entry before modifying any field that participates in equals() or hashCode(), then modify the key and reinsert it. Ideally, though, HashMap keys should be immutable.

----------------------------------------
What is the difference between HashMap, Hashtable, and ConcurrentHashMap?
HashMap is not thread-safe and allows one null key and multiple null values. 
Hashtable is a legacy synchronized collection; its operations are synchronized, which can create significant contention when multiple threads access it. It doesn't allow null keys or values. 
ConcurrentHashMap is designed for concurrent applications and provides thread-safe operations with much better concurrency by avoiding a single global lock for the entire map. It also doesn't allow null keys or values. For a high-concurrency application, I would generally choose ConcurrentHashMap rather than Hashtable.
----------------------------------------
Consider:
Map<String, Integer> map = new ConcurrentHashMap<>();

map.put("count", 0);

for (int i = 0; i < 10; i++) {
    new Thread(() -> {
        map.put("count", map.get("count") + 1);
    }).start();
}

Is this code thread-safe? Will the final value always be 10? Why or why not? How would you fix it?
The problem is this line:
map.put("count", map.get("count") + 1);

It contains two separate operations:
1. get("count")
2. put("count", value + 1)

ConcurrentHashMap makes each individual operation thread-safe, but the combination is not atomic.
For this specific use case:
map.merge("count", 1, Integer::sum);
Or:
map.compute("count", (key, value) -> value + 1);
These perform the update atomically for the key.

ConcurrentHashMap is thread-safe at the individual operation level, but get() followed by put() is not an atomic compound operation. Multiple threads can read the same value and overwrite each other's updates. So the final value is not guaranteed to be 10. I would use compute() or merge() for an atomic map update, or use AtomicInteger if the requirement is simply maintaining a counter."
-----------------------------------------------

What is the difference between synchronized, volatile, and AtomicInteger?
**synchronized** provides mutual exclusion and visibility. It ensures that only one thread can execute the synchronized critical section at a time for the same monitor, and changes are visible to other threads acquiring that monitor.
**volatile** is mainly used for visibility. When one thread updates a volatile variable, other threads can see the latest value, but volatile doesn't provide atomicity for compound operations such as count++.
**AtomicInteger** provides atomic operations such as increment, decrement and compare-and-set using non-blocking concurrency mechanisms. 
I would use volatile for simple state flags, 
synchronized when I need to protect a critical section involving multiple operations, and 
AtomicInteger for atomic counters or similar operations.

--------------------------------------------------------------------
Imagine your Spring Boot application has 200 request threads, and each request calls a slow external service.
The external service sometimes takes 30 seconds to respond.
What problems can this cause in your application, and how would you design the solution?
200 request threads
       ↓
External Service
       ↓
takes 30 seconds

Request 1 → WAIT 30 sec
Request 2 → WAIT 30 sec
Request 3 → WAIT 30 sec
...
Request 200 → WAIT 30 sec


This can lead to:
- Thread pool exhaustion
- Increased response time
- Request timeouts
- Cascading failures
- High memory/resource usage
- Eventually application failure

If 200 request threads are blocked waiting for a slow downstream service for 30 seconds, we can exhaust the application thread pool. This increases latency, causes request timeouts, and can eventually result in cascading failures.
I would first configure an appropriate connection and request timeout so that we don't wait indefinitely. 
Then I would use a circuit breaker to stop calling the downstream service when failures cross a threshold.
For transient failures, I would use limited retries with exponential backoff and jitter. 
I would also isolate resources using a bulkhead approach so that one slow dependency doesn't consume all application threads. Depending on the business requirement, I would provide a fallback response or degrade functionality gracefully. Finally, I would monitor downstream latency, error rate, timeout rate, thread-pool utilization and circuit-breaker state.

**Slow dependency → threads blocked → timeout → circuit breaker → retry carefully → bulkhead → fallback → monitoring**
--------------------------------------------------------
Creating a new thread for every task is not scalable because threads have memory and scheduling overhead. If the application receives a large number of tasks, creating thousands of threads can cause excessive memory consumption, context switching and CPU overhead, and can eventually exhaust system resources.
I would use a thread pool through ExecutorService or ThreadPoolExecutor. The pool maintains a controlled number of worker threads and processes submitted tasks from a queue. This allows us to control concurrency, reuse threads and protect the application from creating an unbounded number of threads.
The pool size should be selected based on whether the workload is CPU-bound or I/O-bound, along with CPU cores, expected latency and throughput requirements.
---------------------------------------------------------
How do you decide thread-pool size?
It depends on whether the workload is CPU-bound or I/O-bound. For CPU-bound tasks, I would generally start around the number of available CPU cores because excessive threads can cause context switching. For I/O-bound tasks, I can use a larger pool because threads spend significant time waiting for external resources. However, I wouldn't choose the size arbitrarily; I would consider I/O latency, throughput, CPU utilization, memory, downstream limits such as database connection pools, and validate the configuration through load testing.

new ThreadPoolExecutor(
    10,     // ?
    50,     // ?
    60,     // ?
    TimeUnit.SECONDS,
    queue   // ?
);

**What are corePoolSize, maximumPoolSize, keepAliveTime, and the queue used for?**
With a typical bounded queue:
Core threads → Queue fills → Additional threads → Maximum reached → Rejection
corePoolSize defines the normal number of worker threads. 
maximumPoolSize defines the upper limit of threads that can be created. 
The work queue holds tasks waiting for execution when workers are busy.
keepAliveTime controls how long idle non-core threads are kept before being removed. 
Together these parameters allow us to control concurrency and resource usage."

new ThreadPoolExecutor(
    10,                         // corePoolSize
    50,                         // maximumPoolSize
    60,                         // keepAliveTime
    TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100)
);
How many tasks will be running immediately?
How many will be waiting in the queue?
When will the additional threads beyond the 10 core threads be created?
What happens if 151 tasks arrive and all existing threads are busy?

**Suppose 150 tasks arrive almost simultaneously.**
"With a core size of 10 and a queue capacity of 100, the first 10 tasks are executed by core threads. The next 100 tasks are queued. Once the queue becomes full, the executor creates additional threads up to the maximum pool size of 50. Therefore, for 150 tasks, 50 can be running and 100 can be queued. If another task arrives while all 50 threads are busy and the queue is full, the rejection policy is triggered."

**"What is the difference between execute() and submit() in ExecutorService?"**
Key difference
**execute()	                            submit()**
Accepts Runnable	                    Accepts Runnable or Callable
Returns nothing (void)	                Returns a Future
Mainly for fire-and-forget execution	Lets you track/result/cancel the task
Exception handling differs	            Exceptions can be retrieved through Future.get()

executor.execute(() -> doWork());      Future<Integer> future =
                                                    executor.submit(() -> calculateSomething());
                                                Integer result = future.get();

execute() is used to submit a Runnable when I don't need a result. It returns void. 
submit() can accept Runnable or Callable and returns a Future, which allows me to get the result, check completion, cancel the task, or handle exceptions through the Future."


