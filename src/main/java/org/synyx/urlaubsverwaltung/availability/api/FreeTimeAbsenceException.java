package org.synyx.urlaubsverwaltung.availability.api;

/**
 * Exception that is thrown when no valid WorkingTime can be found for a period.
 */
public class FreeTimeAbsenceException extends IllegalStateException {

    private final String message;

    FreeTimeAbsenceException(String message) {

        super(message);

        this.message = message;
    }

    @Override
    public String getMessage() {

        return message;
    }
}
