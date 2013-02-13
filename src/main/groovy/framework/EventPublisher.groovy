package framework

import domain.events.EventEnvelope

public interface EventPublisher {

    void publish(EventEnvelope eventEnvelope)

}
