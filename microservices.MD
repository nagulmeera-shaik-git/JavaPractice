What is a distributed transaction and how do you handle it in microservices?", I would say:

A distributed transaction is a business operation that involves multiple independent resources, typically databases belonging to different microservices. A normal @Transactional transaction cannot automatically roll back changes made in another service. Earlier we could use approaches like two-phase commit, but in microservices we commonly use Saga, where the business transaction is divided into local transactions and failures are handled through compensating actions. For example, in an order flow, if payment succeeds but inventory reservation fails, we can refund the payment and cancel the order. In production, I would also use idempotency, persistent workflow state, retries and reliable event publishing because network failures and application crashes can happen between any two steps

--------------------------------------------------------------------------------

Distributed transaction → one business operation across multiple independent resources.

@Transactional → does not automatically manage transactions in other microservices.

2PC → coordinator + prepare + commit.

Saga → local transactions + compensation.

Compensation is not database rollback.

Network timeout does not always mean operation failed.

Idempotency is important for safe retries.

Outbox helps make database change + event publishing reliable.

Persist workflow state; don't depend only on memory.

Compensation can also fail, so retry and reconciliation are required.

---------------------------------------------------------------------------------------

1. Why do we need the Outbox Pattern?
Suppose Order Service has to do two things:

Save the order in the database.
Publish an OrderCreated event to Kafka.
The simple code may look like this:

JAVA

public void createOrder(Order order) {
    orderRepository.save(order);
    kafkaTemplate.send("order-created", order.getId());
}

Looks fine, right?

But there is a small production problem.
What if the database save succeeds but Kafka publishing fails?

Order DB       → SUCCESS
Kafka          → FAILED

Now the order is present in the database, but Payment/Inventory service never received the event.

This is where the Outbox Pattern helps.

Outbox Pattern is a way to reliably publish an event after a database transaction by first saving the event in an Outbox table in the same database transaction as the business data.
