1. First: the JVM memory picture

Think of the JVM like this:

                 JAVA APPLICATION
                       │
                       ▼
              ┌──────────────────┐
              │       JVM         │
              │                   │
              │  ┌─────────────┐  │
              │  │ HEAP        │  │
              │  │             │  │
              │  │ Young Gen   │  │
              │  │  ├─ Eden    │  │
              │  │  └─ Survivor│  │
              │  │             │  │
              │  │ Old Gen     │  │
              │  └─────────────┘  │
              │                   │
              │  Metaspace        │
              │                   │
              │  Thread Stacks    │
              │                   │
              │  Code Cache       │
              └──────────────────┘

For interviews, remember 4 major areas:

Heap → Metaspace → Stack → Native/Off-heap

But Heap is the most important one when discussing GC and memory leaks.

2. Heap — where most Java objects live
Suppose you write:
Employee e = new Employee();
**The Employee object is normally created on the heap.**
Reference ≠ Object
The reference(e) may be on a thread's stack, while the object is on the heap.
**Garbage Collection(GC)**
GC identifies objects that are no longer reachable from GC roots and reclaims their memory.

4. GC Roots — very important for interviews

How does JVM know whether an object is still alive?
It starts from GC Roots.
include
Local variables/reference from active thread stacks
Static references
Active threads
JNI/native references
Other JVM-managed roots


**GC doesn't care whether an object has references. It cares whether the object is reachable from GC Roots.**
**An object is eligible for GC when there is no path from any GC Root to that object.**

GC determines liveness based on reachability from GC Roots, not simply whether an object has references. 
**A memory leak occurs when objects that are logically no longer needed remain strongly reachable through an unintended reference path from a GC Root.**

**Young Generation — the most important concept**

Heap is traditionally divided into:

HEAP
│
├── Young Generation
│     ├── Eden
│     └── Survivor
│
└── Old Generation

Most objects are created in Eden.


Application is getting slower after several hours, memory is high and GC is frequent. What do you suspect?
I would suspect memory pressure due to excessive allocation or object retention, potentially a memory leak. I would first analyze GC behavior and heap-after-GC trends; if Old Gen keeps growing, I'd take a heap dump and identify the objects retaining memory.

Why are so many objects still alive?

Possible causes:
Memory leak
Unbounded cache
Static collection
Objects retained unnecessarily
Long-lived sessions
ThreadLocal retention
Application workload genuinely requiring more memory






Your mental model for production debugging

I want you to memorize this flow rather than individual definitions:

                 PRODUCTION SLOWDOWN
                         │
          ┌──────────────┼──────────────┐
          ↓              ↓              ↓
         CPU           Memory           I/O
          │              │
          │              ↓
          │            GC?
          │              │
          │       ┌──────┴──────┐
          │       ↓             ↓
          │   Frequent GC    Heap growth
          │       │             │
          │       ↓             ↓
          │   GC logs        Heap dump
          │                     │
          ↓                     ↓
      Thread dump          Find retained objects

That's how an experienced engineer should approach it.

Why is the application becoming slower after several hours?
I'd first determine whether we're seeing high allocation or object retention. I'd check GC frequency, pause times, and heap usage before and after GC. If the heap drops significantly after GC, it may be allocation pressure. If Old Gen continues growing and the post-GC baseline keeps increasing, I'd suspect object retention or a memory leak and investigate with a heap dump.

             OBJECT CREATION
                    │
                    ▼
                  EDEN
                    │
                 Young GC
              ┌─────┴─────┐
              │           │
            DEAD        ALIVE
              │           │
              ▼           ▼
            GONE      Survivor
                          │
                     survives
                          │
                          ▼
                      OLD GEN
                          │
                    ┌─────┴─────┐
                    │           │
              reclaimable    still alive
                    │           │
                    ▼           ▼
                   FREE     retained objects

Allocation → GC → Reclamation → Retention


What is G1 GC?
G1, or Garbage-First Garbage Collector, divides the heap into regions rather than treating Young and Old generations as large contiguous spaces. 
It identifies regions with a high amount of reclaimable garbage and prioritizes them for collection. 
It performs young and mixed collections and uses concurrent phases to help manage large heaps while targeting predictable pause times.



"How would you troubleshoot a Java memory leak?
First I'd confirm whether the heap is actually growing over time and whether the post-GC heap baseline keeps increasing. I'd analyze GC logs and JVM metrics to distinguish high allocation from object retention. If retention is suspected, I'd capture a heap dump and analyze the largest retained objects and their GC-root paths. Then I'd identify the application-level reference causing the objects to remain reachable—for example an unbounded cache, static collection, ThreadLocal, listener, or similar long-lived reference.

memory-leak formula
OBJECT
   ↓
STILL REFERENCED
   ↓
GC CANNOT COLLECT
   ↓
MEMORY RETAINED
   ↓
OLD GEN GROWS
   ↓
GC PRESSURE
   ↓
SLOW APPLICATION
   ↓
POSSIBLE OOM

Who is keeping this object alive?"

When debugging a memory leak, that question will lead you toward the answer.
Next we'll learn Heap Dump analysis in a practical example—I'll show you how you'd go from "memory is 90%" → heap dump → HashMap → GC Root → actual leak, step by step.


Scenario: Your Spring Boot application suddenly gets:
java.lang.OutOfMemoryError: Java heap space Application memory keeps increasing until the crash.
What are the first 2–3 things you'd investigate?

First, I’ll check JVM heap and GC logs to understand whether the heap is continuously growing and whether GC is unable to reclaim memory. Then I’ll take a heap dump and analyze retained objects to identify a possible memory leak. I’ll also check recent deployments, traffic increase, or unbounded caches/collections.
**OOM → GC logs → Heap Dump → Find retained objects → Root cause**

if you find a huge HashMap or cache retaining millions of objects, that points toward a memory leak/unbounded cache.

Your application is not crashing, but response time gradually increases:

Initially: 200 ms
After several hours: 3–5 seconds
CPU: 40%
Memory: 85%
GC: happening very frequently

Interviewer: What would you suspect, and what would you check first?
The first thing I would suspect is memory pressure, possibly a memory leak or excessive object creation, because memory is already at 85% and GC is happening very frequently. Frequent GC can increase response time even though CPU is only 40%

Since latency increases gradually over several hours, memory is 85%, and GC is very frequent, my first suspicion would be memory pressure caused by excessive object allocation or a memory leak. I would first analyze GC logs and heap usage, especially Old Gen behavior. If memory continues growing after GC, I would take a heap dump and identify the objects retaining memory.

Slowly getting worse + high memory + frequent GC → suspect memory leak / excessive allocation → check GC → Old Gen → heap dump.

