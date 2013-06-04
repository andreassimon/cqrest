package de.oneos.eventsourcing.orders

import de.oneos.eventsourcing.BaseEvent

class Order_line_was_added extends BaseEvent<Order> {
    UUID article

    Order_line_was_added(UUID article) {
        this.article = article
    }
}
