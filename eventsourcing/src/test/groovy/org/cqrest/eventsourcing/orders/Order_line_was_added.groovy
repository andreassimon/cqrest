package org.cqrest.eventsourcing.orders

import org.cqrest.eventsourcing.BaseEvent

class Order_line_was_added extends BaseEvent {
    UUID article

    Order_line_was_added(UUID article) {
        this.article = article
    }
}
