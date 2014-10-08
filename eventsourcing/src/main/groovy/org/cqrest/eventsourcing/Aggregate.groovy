package org.cqrest.eventsourcing

import java.lang.annotation.*
import org.codehaus.groovy.transform.GroovyASTTransformationClass


@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["org.cqrest.eventsourcing.AggregateTransformation"])
public @interface Aggregate {

}
