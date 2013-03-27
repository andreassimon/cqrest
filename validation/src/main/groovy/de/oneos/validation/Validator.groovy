package de.oneos.validation

public interface Validator<T> {

    boolean isSatisfiedBy(T candidate)

    String validationMassage(T candidate)

}
