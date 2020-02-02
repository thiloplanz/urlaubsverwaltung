package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@RunWith(MockitoJUnitRunner.class)
public class CalendarMailServiceTest {

    private CalendarMailService sut;

    @Mock
    private MailService mailService;

    @Before
    public void setUp() {
        sut = new CalendarMailService(mailService);
    }

    @Test
    public void sendRejectedNotification() {

        final String calendarName = "calendar name";
        final String exception = "Some exception";

        final CalendarSettings calendarSettings = new CalendarSettings();
        final AbsenceTimeConfiguration absenceTimeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        final LocalDate startDate = LocalDate.of(2019, 5, 5);
        final LocalDate endDate = LocalDate.of(2019, 5, 10);
        final Period period = new Period(startDate, endDate, FULL);

        final Absence absence = new Absence(new Person(), period, absenceTimeConfiguration);

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("exception", exception);

        sut.sendCalendarSyncErrorNotification(calendarName, absence, exception);

        verify(mailService).sendTechnicalMail("subject.error.calendar.sync", "error_calendar_sync", model);
    }


    @Test
    public void sendCalendarUpdateErrorNotification() {

        final String calendarName = "calendar name";
        final String exception = "Some exception";
        final String eventId = "eventId";

        final CalendarSettings calendarSettings = new CalendarSettings();
        final AbsenceTimeConfiguration absenceTimeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        final LocalDate startDate = LocalDate.of(2019, 5, 5);
        final LocalDate endDate = LocalDate.of(2019, 5, 10);
        final Period period = new Period(startDate, endDate, FULL);

        final Absence absence = new Absence(new Person(), period, absenceTimeConfiguration);

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("eventId", eventId);
        model.put("exception", exception);

        sut.sendCalendarUpdateErrorNotification(calendarName, absence, eventId, exception);

        verify(mailService).sendTechnicalMail("subject.error.calendar.update", "error_calendar_update", model);
    }

    @Test
    public void sendCalendarDeleteErrorNotification() {

        final String calendarName = "calendar name";
        final String exception = "Some exception";
        final String eventId = "eventId";

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("eventId", eventId);
        model.put("exception", exception);

        sut.sendCalendarDeleteErrorNotification(calendarName, eventId, exception);

        verify(mailService).sendTechnicalMail("subject.error.calendar.delete", "error_calendar_delete", model);
    }
}
