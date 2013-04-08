package de.oneos.eventsourcing.orders

import de.oneos.eventsourcing.BaseEvent

class Order_was_created extends BaseEvent<Order> {
    void applyTo(Order order) {
        order
    }
}