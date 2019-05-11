package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.sicknote.SickNoteInteractionServiceImpl}.
 */
public class SickNoteInteractionServiceImplTest {

    private SickNoteInteractionService sickNoteInteractionService;

    private SickNoteService sickNoteService;
    private SickNoteCommentService commentService;
    private ApplicationInteractionService applicationInteractionService;
    private CalendarSyncService calendarSyncService;
    private AbsenceMappingService absenceMappingService;
    private SettingsService settingsService;

    private SickNote sickNote;
    private Person person;
    private AbsenceMapping absenceMapping;

    @Before
    public void setUp() {

        sickNoteService = mock(SickNoteService.class);
        commentService = mock(SickNoteCommentService.class);
        applicationInteractionService = mock(ApplicationInteractionService.class);
        calendarSyncService = mock(CalendarSyncService.class);
        absenceMappingService = mock(AbsenceMappingService.class);
        settingsService = mock(SettingsService.class);

        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        absenceMapping = new AbsenceMapping(1, AbsenceType.VACATION, "42");
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE)))
            .thenReturn(Optional.of(absenceMapping));
        when(settingsService.getSettings()).thenReturn(new Settings());

        sickNoteInteractionService = new SickNoteInteractionServiceImpl(sickNoteService, commentService,
                applicationInteractionService, calendarSyncService, absenceMappingService, settingsService);

        sickNote = new SickNote();
        sickNote.setId(42);
        sickNote.setStatus(SickNoteStatus.ACTIVE);
        sickNote.setStartDate(LocalDate.now(UTC));
        sickNote.setEndDate(LocalDate.now(UTC));
        sickNote.setDayLength(DayLength.FULL);
        sickNote.setPerson(TestDataCreator.createPerson());

        person = TestDataCreator.createPerson();
    }


    @Test
    public void ensureCreatedSickNoteIsPersisted() {

        SickNote createdSickNote = sickNoteInteractionService.create(sickNote, person);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.CREATED, Optional.empty(), person);

        Assert.assertNotNull("Should not be null", createdSickNote);

        Assert.assertNotNull("Last edited date should be set", createdSickNote.getLastEdited());
        Assert.assertEquals("Wrong status", SickNoteStatus.ACTIVE, createdSickNote.getStatus());
    }


    @Test
    public void ensureCreatingSickNoteAddsEventToCalendar() {

        sickNoteInteractionService.create(sickNote, person);

        verify(calendarSyncService).addAbsence(any(Absence.class));
        verify(absenceMappingService)
            .create(eq(sickNote.getId()), eq(AbsenceType.SICKNOTE), anyString());
    }


    @Test
    public void ensureUpdatedSickNoteIsPersisted() {

        SickNote updatedSickNote = sickNoteInteractionService.update(sickNote, person);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.EDITED, Optional.empty(), person);

        Assert.assertNotNull("Should not be null", updatedSickNote);

        Assert.assertNotNull("Last edited date should be set", updatedSickNote.getLastEdited());
        Assert.assertEquals("Wrong status", SickNoteStatus.ACTIVE, updatedSickNote.getStatus());
    }


    @Test
    public void ensureUpdatingSickNoteUpdatesCalendarEvent() {

        sickNoteInteractionService.update(sickNote, person);

        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE));
    }


    @Test
    public void ensureCancelledSickNoteIsPersisted() {

        SickNote cancelledSickNote = sickNoteInteractionService.cancel(sickNote, person);

        verify(sickNoteService).save(sickNote);
        verify(commentService).create(sickNote, SickNoteAction.CANCELLED, Optional.empty(), person);

        Assert.assertNotNull("Should not be null", cancelledSickNote);

        Assert.assertNotNull("Last edited date should be set", cancelledSickNote.getLastEdited());
        Assert.assertEquals("Wrong status", SickNoteStatus.CANCELLED, cancelledSickNote.getStatus());
    }


    @Test
    public void ensureCancellingSickNoteDeletesCalendarEvent() {

        sickNoteInteractionService.cancel(sickNote, person);

        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE));
        verify(calendarSyncService).deleteAbsence(anyString());
        verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureConvertedSickNoteIsPersisted() {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(TestDataCreator.createPerson());

        SickNote convertedSickNote = sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        // assert sick note correctly updated

        verify(sickNoteService).save(sickNote);
        verify(commentService)
            .create(sickNote, SickNoteAction.CONVERTED_TO_VACATION, Optional.empty(), person);

        Assert.assertNotNull("Should not be null", convertedSickNote);

        Assert.assertNotNull("Last edited date should be set", convertedSickNote.getLastEdited());
        Assert.assertEquals("Wrong status", SickNoteStatus.CONVERTED_TO_VACATION, convertedSickNote.getStatus());

        // assert application for leave correctly created
        verify(applicationInteractionService).createFromConvertedSickNote(applicationForLeave, person);
    }


    @Test
    public void ensureConvertingSickNoteToVacationUpdatesCalendarEvent() {

        Application applicationForLeave = new Application();
        applicationForLeave.setStartDate(LocalDate.now(UTC));
        applicationForLeave.setEndDate(LocalDate.now(UTC));
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setPerson(person);

        sickNoteInteractionService.convert(sickNote, applicationForLeave, person);

        verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.SICKNOTE));
        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).delete(eq(absenceMapping));
        verify(absenceMappingService).create(isNull(), eq(AbsenceType.VACATION), anyString());
    }
}
