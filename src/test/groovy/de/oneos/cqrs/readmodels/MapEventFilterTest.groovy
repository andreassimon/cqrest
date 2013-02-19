package de.oneos.cqrs.readmodels

import org.junit.Test
import org.junit.Ignore

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Description

import static org.hamcrest.Matchers.not
import org.junit.Before

class MapEventFilterTest {

    def eventConstraints = [
        applicationName: 'APPLICATION NAME',
        boundedContextName: 'BOUNDED CONTEXT NAME',
        aggregateName: 'AGGREGATE NAME',
        eventName: 'AN EVENT NAME'
    ]

    def matchingEvent

    def eventFilter = new MapEventFilter(eventConstraints)

    @Before
    void setUp() {
        def emc = new ExpandoMetaClass(LinkedHashMap)
        emc.but = { Map differingAttributes ->
            delegate + differingAttributes
        }
        emc.initialize()

        matchingEvent = new LinkedHashMap(eventConstraints)
        matchingEvent.metaClass = emc
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
    void should_not_match_events_with_a_different_bounded_context_name() {
        assertThat eventFilter, not(matches(matchingEvent.but(boundedContextName: 'A DIFFERENT BOUNDED CONTEXT NAME')))
    }

    @Test
    void should_not_match_events_with_a_different_aggregate_name() {
        assertThat eventFilter, not(matches(matchingEvent.but(aggregateName: 'A DIFFERENT AGGREGATE NAME')))
    }

    @Test
    void should_not_match_events_with_a_different_event_name() {
        assertThat eventFilter, not(matches(matchingEvent.but(eventName: 'A DIFFERENT EVENT NAME')))
    }
}
