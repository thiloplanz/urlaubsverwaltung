package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;

@RunWith(MockitoJUnitRunner.class)
public class SickNoteCronMailServiceTest {

    private SickNoteCronMailService sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private MailService mailService;

    @Before
    public void setUp() {
        sut = new SickNoteCronMailService(settingsService, sickNoteService, mailService);
    }

    @Test
    public void ensureSendEndOfSickPayNotification() {

        final Person person = new Person();
        person.setLoginName("Hulk");

        final SickNote sickNoteA = new SickNote();
        sickNoteA.setId(1);
        sickNoteA.setPerson(person);

        final SickNote sickNoteB = new SickNote();
        sickNoteB.setId(2);
        sickNoteB.setPerson(person);

        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(asList(sickNoteA, sickNoteB));

        prepareSettingsWithMaximumSickPayDays(5);

        Map<String, Object> modelA = new HashMap<>();
        modelA.put("maximumSickPayDays", 5);
        modelA.put("sickNote", sickNoteA);

        Map<String, Object> modelB = new HashMap<>();
        modelB.put("maximumSickPayDays", 5);
        modelB.put("sickNote", sickNoteB);

        sut.sendEndOfSickPayNotification();

        verify(mailService).sendMailTo(sickNoteA.getPerson(), "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelA);
        verify(mailService).sendMailTo(NOTIFICATION_OFFICE, "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelA);

        verify(mailService).sendMailTo(sickNoteB.getPerson(), "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelB);
        verify(mailService).sendMailTo(NOTIFICATION_OFFICE, "subject.sicknote.endOfSickPay", "sicknote_end_of_sick_pay", modelB);
    }

    @Test
    public void ensureNoSendWhenDeactivated() {

        boolean isInactive = false;
        prepareSettingsWithRemindForWaitingApplications(isInactive);

        sut.sendEndOfSickPayNotification();
        verifyZeroInteractions(mailService);
    }


    private void prepareSettingsWithRemindForWaitingApplications(Boolean isActive) {
        Settings settings = new Settings();
        AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setRemindForWaitingApplications(isActive);
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private void prepareSettingsWithMaximumSickPayDays(Integer sickPayDays) {
        Settings settings = new Settings();
        AbsenceSettings absenceSettings = new AbsenceSettings();
        absenceSettings.setMaximumSickPayDays(sickPayDays);
        settings.setAbsenceSettings(absenceSettings);
        when(settingsService.getSettings()).thenReturn(settings);
    }
}
