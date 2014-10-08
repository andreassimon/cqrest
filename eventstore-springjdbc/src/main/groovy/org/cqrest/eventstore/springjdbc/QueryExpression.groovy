package org.cqrest.eventstore.springjdbc

class QueryExpression {


    String forCriteria(Map<String, ?> criteria) {
        """\
SELECT *
FROM ${Schema.TABLE_NAME}
${whereClause(criteria)}
ORDER BY ${Schema.AGGREGATE_ID}, ${Schema.SEQUENCE_NUMBER};\
""".toString()
    }

    protected final static Map<String, String> COLUMN_NAME = [
      boundedContextName: Schema.BOUNDED_CONTEXT_NAME,
      aggregateName: Schema.AGGREGATE_NAME,
      aggregateId: Schema.AGGREGATE_ID,
      eventName: Schema.EVENT_NAME
    ]

    protected whereClause(Map<String, ?> criteria) {
        if(criteria.isEmpty()) { return '' }
        'WHERE ' + criteria.collect { attribute, values -> condition(attribute, values) }.join(' AND ')
    }

    protected condition(String attribute, values) {
        switch(values.getClass()) {
            case [String, UUID]:
                return "${COLUMN_NAME[attribute]} = :$attribute".toString()
            case List:
                return "${Schema.EVENT_NAME} IN (:$attribute)".toString()
            default:
                throw new RuntimeException("Cannot transform ($attribute, ${values.getClass().simpleName}<$values>) to WHERE clause")
        }
    }

}
