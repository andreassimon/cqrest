package de.oneos.validation

public interface Validatable<T> {

    boolean isValid()

    String validationMessage()

}
