package org.synyx.urlaubsverwaltung.web.sicknote;

/**
 * Thrown in case trying to execute action on an already inactive sick note.
 */
public class SickNoteAlreadyInactiveException extends Exception {

    public SickNoteAlreadyInactiveException(Integer id) {

        super("Sick note with ID = " + id + " is already inactive.");
    }
}
