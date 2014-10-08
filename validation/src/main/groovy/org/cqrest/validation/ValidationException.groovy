package org.cqrest.validation


class ValidationException extends RuntimeException {
    ValidationException(invalidObject, String validationMessage) {
        super("'$invalidObject' is invalid! Message is '$validationMessage'")
    }
}
