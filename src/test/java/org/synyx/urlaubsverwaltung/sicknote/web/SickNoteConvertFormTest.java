package org.synyx.urlaubsverwaltung.sicknote.web;

import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createSickNote;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createVacationType;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.sicknote.web.SickNoteConvertForm}.
 */
public class SickNoteConvertFormTest {

    @Test
    public void ensureCorrectProvidedValuesFromSickNote() {

        final Person person = createPerson();
        final LocalDate startDate = LocalDate.of(2014, 1, 1);
        final LocalDate endDate = LocalDate.of(2014, 1, 10);
        final SickNote sickNote = createSickNote(person, startDate, endDate, DayLength.NOON);

        final SickNoteConvertForm sut = new SickNoteConvertForm(sickNote);
        assertThat(sut.getPerson()).isNotNull();
        assertThat(sut.getDayLength()).isNotNull();
        assertThat(sut.getStartDate()).isNotNull();
        assertThat(sut.getEndDate()).isNotNull();

        assertThat(sut.getPerson()).isEqualTo(person);
        assertThat(sut.getDayLength()).isEqualTo(DayLength.NOON);
        assertThat(sut.getStartDate()).isEqualTo(startDate);
        assertThat(sut.getEndDate()).isEqualTo(endDate);
    }

    @Test
    public void ensureGeneratesBasicApplicationForLeave() {

        final Person person = createPerson();
        final LocalDate startDate = LocalDate.of(2014, 1, 1);
        final LocalDate endDate = LocalDate.of(2014, 1, 10);
        final SickNote sickNote = createSickNote(person, startDate, endDate, FULL);
        final VacationType vacationType = createVacationType(VacationCategory.UNPAIDLEAVE);

        final SickNoteConvertForm sut = new SickNoteConvertForm(sickNote);
        sut.setReason("Some Reason");
        sut.setVacationType(vacationType);

        final Application applicationForLeave = sut.generateApplicationForLeave();
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getVacationType()).isEqualTo(vacationType);
        assertThat(applicationForLeave.getDayLength()).isEqualTo(FULL);
        assertThat(applicationForLeave.getStartDate()).isEqualTo(startDate);
        assertThat(applicationForLeave.getEndDate()).isEqualTo(endDate);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ALLOWED);
        assertThat(applicationForLeave.getReason()).isEqualTo("Some Reason");
        assertThat(applicationForLeave.getApplicationDate()).isNotNull();
        assertThat(applicationForLeave.getEditedDate()).isNotNull();
    }
}
