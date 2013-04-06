package de.oneos.eventsourcing.test

import static java.lang.String.format

class EventAttributeDiff {

    public static final String DEFAULT_ATTRIBUTE_DIFF_FORMAT = '               %-20s    %-10s    %s%n'


    String attributeName
    def leftAttribute, rightAttribute

    EventAttributeDiff(attributeName, leftValue, rightValue) {
        assert attributeName != null

        this.leftAttribute = [value: leftValue]
        this.rightAttribute = [value: rightValue]

        if (valuesAreDifferent()) {
            this.attributeName = "[$attributeName]"
        } else {
            this.attributeName = " $attributeName "
        }
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

    public getLeftValue() {
        return valueOf(leftAttribute)
    }

    public getRightValue() {
        valueOf(rightAttribute)
    }

    protected valueOf(def attribute) {
        if (valuesAreDifferent()) {
            return attribute?.value
        }
        return '='
    }

    protected valuesAreDifferent() {
        leftAttribute?.value != rightAttribute?.value
    }

}
