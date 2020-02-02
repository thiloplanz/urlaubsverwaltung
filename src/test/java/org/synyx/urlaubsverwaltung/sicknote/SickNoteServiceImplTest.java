package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SickNoteServiceImplTest {

    private SickNoteServiceImpl sut;

    @Mock
    private SickNoteDAO sickNoteDAO;
    @Mock
    private SettingsService settingsService;

    @Before
    public void setUp() {
        sut = new SickNoteServiceImpl(sickNoteDAO, settingsService);
    }

    @Test
    public void save() {
        final SickNote sickNote = new SickNote();
        sut.save(sickNote);
        verify(sickNoteDAO).save(sickNote);
    }

    @Test
    public void getById() {
        final Optional<SickNote> sickNote = Optional.of(new SickNote());
        when(sickNoteDAO.findById(1)).thenReturn(sickNote);

        final Optional<SickNote> actualSickNote = sut.getById(1);
        assertThat(actualSickNote).isEqualTo(sickNote);
    }

    @Test
    public void findByPeriod() {
        final LocalDate from = LocalDate.of(2015, 1, 1);
        final LocalDate to = LocalDate.of(2016, 1, 1);
        final SickNote sickNote = new SickNote();
        when(sickNoteDAO.findByPeriod(from, to)).thenReturn(singletonList(sickNote));

        final List<SickNote> sickNotes = sut.getByPeriod(from, to);
        assertThat(sickNotes).contains(sickNote);
    }

    @Test
    public void getAllActiveByYear() {
        final SickNote sickNote = new SickNote();
        when(sickNoteDAO.findAllActiveByYear(2017)).thenReturn(singletonList(sickNote));

        final List<SickNote> sickNotes = sut.getAllActiveByYear(2017);
        assertThat(sickNotes).contains(sickNote);
    }

    @Test
    public void getNumberOfPersonsWithMinimumOneSickNote() {
        when(sickNoteDAO.findNumberOfPersonsWithMinimumOneSickNote(2017)).thenReturn(5L);

        final Long numberOfPersonsWithMinimumOneSickNote = sut.getNumberOfPersonsWithMinimumOneSickNote(2017);
        assertThat(numberOfPersonsWithMinimumOneSickNote).isSameAs(5L);
    }

    @Test
    public void getSickNotesReachingEndOfSickPay() {

        final AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setMaximumSickPayDays(5);

        final Settings settings = new Settings();
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);

        final SickNote sickNote = new SickNote();
        when(sickNoteDAO.findSickNotesByMinimumLengthAndEndDate(eq(5), any(LocalDate.class))).thenReturn(singletonList(sickNote));

        final List<SickNote> sickNotesReachingEndOfSickPay = sut.getSickNotesReachingEndOfSickPay();
        assertThat(sickNotesReachingEndOfSickPay).contains(sickNote);
    }
}
