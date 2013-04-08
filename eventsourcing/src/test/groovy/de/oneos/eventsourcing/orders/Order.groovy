package de.oneos.eventsourcing.orders

import de.oneos.eventsourcing.Aggregate
import de.oneos.eventsourcing.orders.*

@Aggregate
class Order {

    static aggregateName = 'AGGREGATE'

    final UUID id
    def orderLines = []

    static Order create(UUID id) {
        assert id != null
        Order newOrder = new Order(id)
        newOrder.emit(
            new Order_was_created()
        )
        return newOrder
    }

    Order(UUID id) {
        this.id = id
    }

    void addArticles(List<UUID> articles) {
        emit(
            * articles.collect { new Order_line_was_added(it) }
        )
    }
}
