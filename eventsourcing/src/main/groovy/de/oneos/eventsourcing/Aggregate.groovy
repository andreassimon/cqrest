package de.oneos.eventsourcing

import java.lang.annotation.*
import org.codehaus.groovy.transform.GroovyASTTransformationClass


@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["de.oneos.eventsourcing.AggregateTransformation"])
public @interface Aggregate {

}
