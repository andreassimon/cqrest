package de.oneos.eventselection

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat


class MapEventFilterTest {

    def eventConstraints
    def matchingEvent
    def eventFilter

    @Before
    void setUp() {
        def expandedMapClass = new ExpandoMetaClass(LinkedHashMap)
        expandedMapClass.but = { Map differingAttributes ->
            delegate + differingAttributes
        }
        expandedMapClass.without = { String key ->
            Map truncatedMap = delegate.clone()
            truncatedMap.remove(key)
            return truncatedMap
        }
        expandedMapClass.initialize()

        eventConstraints = new LinkedHashMap(
            applicationName: 'APPLICATION NAME',
            boundedContextName: 'BOUNDED CONTEXT NAME',
            aggregateName: 'AGGREGATE NAME',
            eventName: 'AN EVENT NAME'
        )
        eventConstraints.metaClass = expandedMapClass

        matchingEvent = new LinkedHashMap(eventConstraints)
        matchingEvent.metaClass = expandedMapClass

        eventFilter = new MapEventFilter(eventConstraints)
    }

    @Test
    void is_equal_to_another_MapEventFilter_with_the_same_constraints() {
        def eventConstraints = [eventName: 'AN EVENT NAME']
        def eventFilterA = new MapEventFilter(eventConstraints)
        def eventFilterB = new MapEventFilter(eventConstraints)

        assertThat eventFilterA, equalTo(eventFilterB)
    }

    @Test
    void toString_produces_sensible_output() {
        def eventConstraints = [
            applicationName: 'APPLICATION NAME',
            eventName: 'AN EVENT NAME'
        ]
        def eventFilter = new MapEventFilter(eventConstraints)

        assertThat eventFilter.toString(), equalTo("MapEventFilter<applicationName=='${eventConstraints.applicationName}' && eventName=='${eventConstraints.eventName}'>".toString())
    }

    @Test
    void if_all_attributes_match__it_should_match_the_event() {
        assertThat eventFilter, matches(matchingEvent)
    }

    Matcher matches(event) {
        return new TypeSafeMatcher() {

            @Override
            protected boolean matchesSafely(eventFilter) {
                eventFilter.matches(event)
            }

            @Override
            void describeTo(Description description) {
                description.appendText("matches $event")
            }
        }
    }


    @Test
    void should_not_match_events_with_a_different_application_name() {
        assertThat eventFilter, not(matches(matchingEvent.but(applicationName: 'A DIFFERENT APPLICATION NAME')))
    }

    @Test
    void should_match_events_with_a_different_application_name__if_the_filter_has_a_wildcard_at_application_name() {
        eventFilter = new MapEventFilter(eventConstraints.without('applicationName'))

        assertThat eventFilter, matches(matchingEvent.but(applicationName: 'A DIFFERENT APPLICATION NAME'))
    }

    @Test
    void should_not_match_events_with_a_different_bounded_context_name() {
        assertThat eventFilter, not(matches(matchingEvent.but(boundedContextName: 'A DIFFERENT BOUNDED CONTEXT NAME')))
    }

    @Test
    void should_match_events_with_a_different_bounded_context_name__if_the_filter_has_a_wildcard_at_bounded_context_name() {
        eventFilter = new MapEventFilter(eventConstraints.without('boundedContextName'))

        assertThat eventFilter, matches(matchingEvent.but(boundedContextName: 'A DIFFERENT BOUNDED CONTEXT NAME'))
    }

    @Test
    void should_not_match_events_with_a_different_aggregate_name() {
        assertThat eventFilter, not(matches(matchingEvent.but(aggregateName: 'A DIFFERENT AGGREGATE NAME')))
    }

    @Test
    void should_match_events_with_a_different_aggregate_name__if_the_filter_has_a_wildcard_at_aggregate_name() {
        eventFilter = new MapEventFilter(eventConstraints.without('aggregateName'))

        assertThat eventFilter, matches(matchingEvent.but(aggregateName: 'A DIFFERENT AGGREGATE NAME'))
    }

    @Test
    void should_not_match_events_with_a_different_event_name() {
        assertThat eventFilter, not(matches(matchingEvent.but(eventName: 'A DIFFERENT EVENT NAME')))
    }

    @Test
    void should_match_events_with_a_different_event_name__if_the_filter_has_a_wildcard_at_event_name() {
        eventFilter = new MapEventFilter(eventConstraints.without('eventName'))

        assertThat eventFilter, matches(matchingEvent.but(eventName: 'A DIFFERENT EVENT NAME'))
    }

}
