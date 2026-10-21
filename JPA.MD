JPA/Hibernate uses dirty checking for managed entities. When a managed entity is modified inside a transaction, Hibernate detects the state change during flush and generates the required SQL update. Therefore an explicit save() is not necessarily required for an already-managed entity

N+1 problem
List<Order> orders = orderRepository.findAll();

@ManyToOne
private Customer customer;

for (Order order : orders) {
    System.out.println(order.getCustomer().getName());
}
There are 100 orders.

1 query → fetch orders
100 queries → fetch customers
101 queries

How can we solve it?
Common approaches include:

1. JOIN FETCH
@Query("""
    SELECT o
    FROM Order o
    JOIN FETCH o.customer
""")
List<Order> findOrdersWithCustomers();

This can fetch orders and customers together.

2. Entity graphs
@EntityGraph(attributePaths = "customer")
List<Order> findAll();

3. Batch fetching
Hibernate can fetch related entities in batches rather than one at a time.

Solve it based on the actual access pattern — JOIN FETCH, EntityGraph, batching, or an appropriate query.

-------------------------------------------------------------------
ACID connection
For interviews, remember:

A — Atomicity
All operations succeed, or all are rolled back.

C — Consistency
Database moves from one valid state to another valid state.

I — Isolation
Concurrent transactions shouldn't improperly interfere with each other.

D — Durability
Once committed, data survives failures according to the database's durability guarantees.

-----------------------------------------------------------------
With Spring's default transaction behavior, RuntimeException and Error generally trigger rollback.

But checked exceptions do not automatically trigger rollback by default.

Propagation	Meaning
REQUIRED	Join existing or create new
REQUIRES_NEW	Always create a new transaction
SUPPORTS	Use existing if available
MANDATORY	Must already have a transaction
NOT_SUPPORTED	Execute without transaction