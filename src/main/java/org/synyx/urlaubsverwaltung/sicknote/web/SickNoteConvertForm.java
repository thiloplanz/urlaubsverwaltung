package org.synyx.urlaubsverwaltung.sicknote.web;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;

import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;


/**
 * Represents a form to convert a sick note to vacation.
 */
public class SickNoteConvertForm {

    private Person person;
    private DayLength dayLength;
    private LocalDate startDate;
    private LocalDate endDate;
    private VacationType vacationType;
    private String reason;

    public SickNoteConvertForm() {
        // needed for Spring magic
    }

    public SickNoteConvertForm(SickNote sickNote) {

        this.person = sickNote.getPerson();
        this.dayLength = sickNote.getDayLength();
        this.startDate = sickNote.getStartDate();
        this.endDate = sickNote.getEndDate();
    }

    public Person getPerson() {

        return person;
    }


    public void setPerson(Person person) {

        this.person = person;
    }


    public DayLength getDayLength() {

        return dayLength;
    }


    public void setDayLength(DayLength dayLength) {

        this.dayLength = dayLength;
    }


    public LocalDate getStartDate() {

        return startDate;
    }


    public void setStartDate(LocalDate startDate) {

        this.startDate = startDate;
    }


    public LocalDate getEndDate() {

        return endDate;
    }


    public void setEndDate(LocalDate endDate) {

        this.endDate = endDate;
    }


    public VacationType getVacationType() {

        return vacationType;
    }


    public void setVacationType(VacationType vacationType) {

        this.vacationType = vacationType;
    }


    public String getReason() {

        return reason;
    }


    public void setReason(String reason) {

        this.reason = reason;
    }


    public Application generateApplicationForLeave() {

        final Application applicationForLeave = new Application();

        applicationForLeave.setPerson(person);
        applicationForLeave.setVacationType(vacationType);
        applicationForLeave.setDayLength(dayLength);
        applicationForLeave.setStartDate(startDate);
        applicationForLeave.setEndDate(endDate);
        applicationForLeave.setReason(reason);

        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setApplicationDate(LocalDate.now(UTC));
        applicationForLeave.setEditedDate(LocalDate.now(UTC));

        return applicationForLeave;
    }
}
