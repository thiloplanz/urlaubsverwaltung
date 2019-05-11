package org.synyx.urlaubsverwaltung.core.sicknote;

/**
 * Describes the category of sick note.
 */
public enum SickNoteCategory {

    SICK_NOTE("application.data.sicknotetype.sicknote"),
    SICK_NOTE_CHILD("application.data.sicknotetype.sicknotechild"),
    OTHER("application.data.sicknotetype.other");

    private final String messageKey;

    SickNoteCategory(String messageKey) {

        this.messageKey = messageKey;
    }

    public String getMessageKey() {

        return messageKey;
    }
}
