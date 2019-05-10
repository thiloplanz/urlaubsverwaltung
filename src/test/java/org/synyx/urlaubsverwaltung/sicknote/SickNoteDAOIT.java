package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteDAO;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteStatus;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SickNoteDAOIT {

    @Autowired
    private SickNoteDAO sickNoteDAO;


    @Test
    public void findSickNotesByMinimumLengthAndEndDateLessThanLimitAndWrongStatus() {

        final LocalDate endDate = LocalDate.of(2019, 5, 20);

        final SickNote sickNote = createSickNote(LocalDate.of(2019, 5, 19), endDate, SickNoteStatus.ACTIVE);
        sickNoteDAO.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(LocalDate.of(2019, 5, 10), endDate, SickNoteStatus.CANCELLED);
        sickNoteDAO.save(sickNoteCancelled);

        final List<SickNote> sickNotesByMinimumLengthAndEndDate = sickNoteDAO.findSickNotesByMinimumLengthAndEndDate(2, dm(endDate).toDate());
        assertThat(sickNotesByMinimumLengthAndEndDate).hasSize(0);
        assertThat(sickNotesByMinimumLengthAndEndDate).doesNotContain(sickNote, sickNoteCancelled);
    }

    @Test
    public void findSickNotesByMinimumLengthAndEndDateExactlyOnLimitAndWrongStatus() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 20);

        final SickNote sickNote = createSickNote(startDate, endDate, SickNoteStatus.ACTIVE);
        sickNoteDAO.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(startDate, endDate, SickNoteStatus.CANCELLED);
        sickNoteDAO.save(sickNoteCancelled);

        final List<SickNote> sickNotesByMinimumLengthAndEndDate = sickNoteDAO.findSickNotesByMinimumLengthAndEndDate(1, dm(endDate).toDate());
        assertThat(sickNotesByMinimumLengthAndEndDate).hasSize(1);
        assertThat(sickNotesByMinimumLengthAndEndDate).contains(sickNote);
        assertThat(sickNotesByMinimumLengthAndEndDate).doesNotContain(sickNoteCancelled);
    }

    @Test
    public void findSickNotesByMinimumLengthAndEndDateMoreThanLimitAndWrongStatus() {

        final LocalDate startDate = LocalDate.of(2019, 5, 19);
        final LocalDate endDate = LocalDate.of(2019, 5, 25);

        final SickNote sickNote = createSickNote(startDate, endDate, SickNoteStatus.ACTIVE);
        sickNoteDAO.save(sickNote);

        final SickNote sickNoteCancelled = createSickNote(startDate, endDate, SickNoteStatus.CANCELLED);
        sickNoteDAO.save(sickNoteCancelled);

        final List<SickNote> sickNotesByMinimumLengthAndEndDate = sickNoteDAO.findSickNotesByMinimumLengthAndEndDate(1, dm(endDate).toDate());
        assertThat(sickNotesByMinimumLengthAndEndDate).hasSize(1);
        assertThat(sickNotesByMinimumLengthAndEndDate).contains(sickNote);
        assertThat(sickNotesByMinimumLengthAndEndDate).doesNotContain(sickNoteCancelled);
    }

    private SickNote createSickNote(LocalDate startDate, LocalDate endDate, SickNoteStatus active) {
        final SickNote sickNote = new SickNote();
        sickNote.setStartDate(dm(startDate));
        sickNote.setEndDate(dm(endDate));
        sickNote.setStatus(active);
        return sickNote;
    }

    private static DateMidnight dm(LocalDate date){
        return new DateMidnight(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }
}
