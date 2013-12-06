package de.oneos.validation


class ValidationException extends RuntimeException {
    ValidationException(invalidObject, String validationMessage) {
        super("'$invalidObject' is invalid! Message is '$validationMessage'")
    }
}
