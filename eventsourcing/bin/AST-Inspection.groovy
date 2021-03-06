import org.cqrest.eventsourcing.*

class Order_was_created extends BaseEvent<Order> { }

class Account_was_opened extends BaseEvent<Account> { }

@Aggregate
class Order {

    void emitSomeEvent() {
        emit(new Order_was_created())
    }

}

class Account {
    protected List<Event> _newEvents = []

    public List<Event> getNewEvents() {
        return _newEvents.asImmutable()
    }

    public Account emit(Event[] events) {
        events.each { Event event ->
            _newEvents << event
            this.invokeMethod(event.eventName, event.serializableForm)
        }
        return this
    }

    public void flushEvents() {
        _newEvents.clear()
    }

}

account = new Account()
account.emit(new Account_was_opened())

order = new Order()
assert -1 == order.getVersion()

order.emitSomeEvent()
