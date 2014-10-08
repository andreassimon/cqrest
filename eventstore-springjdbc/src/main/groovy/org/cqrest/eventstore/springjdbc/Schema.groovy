package org.cqrest.eventstore.springjdbc


class Schema {

    public static final String TABLE_NAME = 'events'

    public static final String AGGREGATE_ID = 'aggregate_id'
    public static final String SEQUENCE_NUMBER = 'sequence_number'
    public static final String BOUNDED_CONTEXT_NAME = 'bounded_context_name'
    public static final String AGGREGATE_NAME = 'aggregate_name'
    public static final String EVENT_NAME = 'event_name'
    public static final String CORRELATION_ID = 'correlation_id'
    public static final String APPLICATION_NAME = 'application_name'
    public static final String ATTRIBUTES = 'attributes'
    public static final String USER = 'user'
    public static final String TIMESTAMP = 'timestamp'

}
