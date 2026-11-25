1. What is a Pod

A Pod is the smallest deployable unit in Kubernetes. It wraps one or more containers that are scheduled together on the same node and share a network namespace (one IP address, one port space) and, optionally, storage volumes. Kubernetes never runs a bare container; it always runs a Pod.

2. Difference between a Pod and a Container

A container is a single running process (or process tree) built from an image, managed by a runtime like containerd. A Pod is a Kubernetes-level wrapper around one or more containers that gives them a shared environment: the same IP, the ability to talk over localhost, shared volumes, and a common lifecycle. You can think of the Pod as a “logical host” and the containers as processes running on it.

3. Why Kubernetes uses Pods

Some processes need to be tightly coupled, for example an app and a helper that ships its logs. Pods let them be scheduled, scaled, and restarted as one unit while still being packaged as separate images. Pods also give Kubernetes a uniform abstraction to attach things like IP addresses, volumes, resource limits, and health probes, regardless of which container runtime is underneath.

4. What is a multi-container Pod

A Pod with more than one container. The containers share the network (they reach each other on localhost) and can share volumes. It’s used only when containers genuinely must live together; unrelated services should be in separate Pods so they can scale independently. Common patterns are sidecar, ambassador (a local proxy to external services), and adapter (reformats the main container’s output).

yaml
    spec:
    containers:
        - name: app
        image: my-app:1.0
        - name: log-shipper
        image: fluent-bit:3.0

5. What is a sidecar container

A helper container that runs alongside the main application container to add functionality without changing the app. Examples include log shippers, service-mesh proxies like Envoy in Istio, config reloaders, and TLS proxies. Since Kubernetes 1.29 there’s native sidecar support: you declare it as an init container with restartPolicy: Always, which guarantees it starts before the main container and keeps running alongside it.

6. Why Pods are ephemeral

Pods are designed to be disposable. They aren’t repaired or moved; when a node fails, a Pod is evicted, or you roll out a new version, the old Pod is deleted and a controller (Deployment, StatefulSet, etc.) creates a new one, usually with a new name and IP. This makes the system self-healing and easy to scale, but it means you shouldn’t rely on a Pod’s IP or local filesystem. Use Services for stable networking and PersistentVolumes for data that must survive.

7. How Pods communicate

Containers inside the same Pod talk over localhost. Between Pods, Kubernetes networking (implemented by a CNI plugin like Calico or Cilium) gives every Pod its own IP, and any Pod can reach any other Pod directly without NAT. Because Pod IPs change, real applications communicate through a Service, which provides a stable virtual IP and DNS name (e.g. my-service.my-namespace.svc.cluster.local) and load-balances across matching Pods. NetworkPolicies can restrict which Pods are allowed to talk to each other.

------------------------------------------------------------------

Question: Why are Deployments important in Kubernetes?

Answer: Deployments automate pod lifecycle management including scaling, rolling updates, self-healing, replica management, and rollback handling, ensuring high availability and reliable production operations.

------------------------------------------------------------

What Exactly Is Kubernetes Service
Service is stable networking abstraction over Pods.

It provides:

Stable IP
Stable DNS name
Load balancing
Pod discovery

-----------------------------------------------

Understanding selector
selector connects Service with matching Pods.

YAML
selector:
  app: payment-service

Service automatically routes traffic to Pods having this label.

Understanding port vs targetPort
Field	        Meaning
port	        Service port
targetPort	    Container application port

Traffic enters Service on port 80 and forwards to Spring Boot container port 8080.

How Load Balancing Works
Suppose Deployment has 3 Pods.

Payment Pod 1
Payment Pod 2
Payment Pod 3
Service distributes incoming requests across Pods automatically.

Kubernetes Services act like internal load balancers.

Real Enterprise Microservices Communication
Spring Boot services communicate using Service names.

http://payment-service
Kubernetes internal DNS resolves Service automatically.

**Real Production Architecture
    Internet
        ↓
    LoadBalancer Service
        ↓
    API Gateway Pods
        ↓
    Internal ClusterIP Services
        ↓
    Microservices Pods
This is common enterprise Kubernetes networking architecture.**

------------------------------------------------------------------------------

Question: Why are Kubernetes Services important in microservices architecture?

Answer: Services provide stable networking, internal service discovery, and load balancing for temporary Pods, enabling reliable communication in distributed microservices systems.


1. How Would You Deploy 100+ Spring Boot Microservices in Production?
Very common architecture-round question.

What Interviewer Checks
Whether candidate understands real enterprise deployment architecture.

Strong answer should include:

Docker images
Kubernetes orchestration
Namespaces
Ingress routing
CI/CD pipelines
Centralized logging
Monitoring
Autoscaling

        GitHub
            ↓
        Jenkins Pipeline
            ↓
        Docker Image Build
            ↓
        Docker Registry
            ↓
        Kubernetes Deployment
            ↓
        Ingress + Services
            ↓
        Monitoring + Scaling

Senior engineers should also discuss environment isolation using namespaces.

--------------------------------------------------------------------------

2. How Would You Handle High Traffic During Big Billion Day Type Sale?
This checks scaling understanding.

Production-Level Answer
Critical microservices like payment and inventory should use Horizontal Pod Autoscaler, Redis caching, Kafka buffering, and database read replicas to absorb traffic spikes.

Strong engineers additionally explain:

        Load balancing
        Stateless services
        Pod autoscaling
        Async communication
        Circuit breakers
        Rate limiting
3. What Happens If Kubernetes Worker Node Crashes?
Very important distributed systems question.

Strong Answer
Kubernetes detects node failure and reschedules Pods automatically to healthy worker nodes using scheduler and controller manager.

You should explain:

    Desired state
    Self-healing
    ReplicaSets
    Traffic rerouting
    High availability
Kubernetes is built assuming infrastructure failures are normal.        

--------------------------------------------------------------------------------------

4. How Would You Achieve Zero Downtime Deployment?
One of the most asked senior deployment questions.

Strong answer should include:

Rolling updates
Readiness probes
Health checks
Blue-Green deployment
Canary deployment
Rollback strategy

kubectl rollout restart deployment payment-service

Readiness probes ensure traffic goes only to healthy Pods.

------------------------------------------------------------------------------

1. Why Pods need stable networking

Pods are ephemeral, so every time one is rescheduled, scaled, or replaced during a rollout, it gets a new IP. If a frontend hard-coded a backend Pod’s IP, it would break the moment that Pod restarted, and it would also have no way to spread traffic across multiple replicas. Clients need a single address that stays the same while the Pods behind it come and go. That’s what a Service provides: a fixed virtual IP and DNS name that always points to the current set of healthy Pods.

2. How Services perform load balancing

A Service uses a label selector to find its Pods. The control plane continuously tracks the IPs of matching Pods that pass their readiness probes and records them in EndpointSlice objects. On every node, kube-proxy watches those EndpointSlices and programs the kernel’s packet rules (iptables, IPVS, or nftables). When a Pod sends traffic to the Service’s virtual IP, those rules rewrite the destination to one of the backend Pod IPs. In iptables mode the backend is picked randomly; IPVS supports algorithms like round-robin and least-connections. This is layer 4 balancing per connection, not per HTTP request, so long-lived connections (like gRPC) can stick to one Pod. Setting sessionAffinity: ClientIP pins a client to the same Pod. Some CNI plugins, like Cilium, replace kube-proxy with eBPF but do the same job.

3. How internal DNS works

The cluster runs a DNS server, usually CoreDNS, as Pods behind a Service in kube-system. The kubelet configures every Pod’s /etc/resolv.conf to point at it, with search domains for the Pod’s namespace. CoreDNS watches the Kubernetes API and automatically creates records for every Service:

<service>.<namespace>.svc.cluster.local  →  Service ClusterIP

Because of the search domains, a Pod in the same namespace can just use my-service, while one in another namespace uses my-service.other-ns. Headless Services (clusterIP: None) return the individual Pod IPs instead of one virtual IP, which StatefulSets use to give each Pod a stable name like db-0.db.my-ns.svc.cluster.local.

4. Difference between ClusterIP and LoadBalancer

ClusterIP is the default type. It gives the Service a virtual IP reachable only from inside the cluster, which is right for internal traffic like an API talking to a database.

LoadBalancer exposes the Service outside the cluster. Kubernetes asks the cloud provider (AWS, GCP, Azure) or a bare-metal implementation like MetalLB to provision an external load balancer with a public or private IP. It builds on the other types: a LoadBalancer Service also gets a ClusterIP and a NodePort, and the external load balancer forwards traffic to the nodes, where kube-proxy routes it to the Pods. Each LoadBalancer Service typically creates a separate cloud load balancer that costs money, so for many HTTP services teams usually expose one Ingress or Gateway controller via a LoadBalancer and route to ClusterIP Services behind it.
