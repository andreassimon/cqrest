package de.oneos.validation

public interface Validator<T> {

    boolean isSatisfiedBy(T instance)

    String validationMassage(T instance)

}
