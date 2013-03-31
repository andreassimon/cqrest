package de.oneos.eventsourcing.test

import static java.lang.String.format

class EventAttributeDiff {

    public static final String DEFAULT_ATTRIBUTE_DIFF_FORMAT = '               %-20s    %-10s    %s%n'


    def leftAttribute, rightAttribute

    EventAttributeDiff(leftAttribute, rightAttribute) {
        this.leftAttribute = leftAttribute
        this.rightAttribute = rightAttribute
    }


    @Override
    String toString() {
        return formatWith(DEFAULT_ATTRIBUTE_DIFF_FORMAT)
    }

    public String formatWith(String attributeDiffFormat) {
        return formatValues(attributeDiffFormat, leftValue, rightValue)
    }

    public String formatValues(String attributeDiffFormat, leftValue, rightValue) {
        format(attributeDiffFormat, attributeName, leftValue, rightValue)
    }

    public String getAttributeName() {
        if(valuesAreDifferent()) {
            return "[$leftAttribute.key]"
        }
        return " $leftAttribute.key "
    }

    public getLeftValue() {
        return valueOf(leftAttribute)
    }

    public getRightValue() {
        valueOf(rightAttribute)
    }

    protected valueOf(def attribute) {
        if (valuesAreDifferent()) {
            return attribute.value
        }
        return '='
    }

    protected valuesAreDifferent() {
        leftAttribute.value != rightAttribute.value
    }

}
