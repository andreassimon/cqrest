package de.oneos.eventstore.springjdbc

import org.junit.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.hamcrest.Matchers.*


class CombinedEventClassResolverTest {

    @Test
    void should_swallow_exceptions_thrown_by_subresolvers() {
        EventClassResolver failingSubresolver = [resolveEvent: { eventName -> throw new ClassNotFoundException() }] as EventClassResolver
        EventClassResolver successfulSubresolver = [resolveEvent: { eventName -> return An_event_happened }] as EventClassResolver

        def combinedResolver = new CombinedEventClassResolver(failingSubresolver, successfulSubresolver)

        assertThat combinedResolver.resolveEvent('An event happened'), equalTo(An_event_happened)
    }

    static class An_event_happened extends de.oneos.eventsourcing.BaseEvent {
        void applyTo(Object aggregate) { }
    }

}

