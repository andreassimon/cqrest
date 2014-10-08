package org.cqrest.validation

public interface Validatable<T> {

    boolean isValid()

    String validationMessage()

}
