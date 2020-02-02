package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.mail.MailService;

import java.util.HashMap;
import java.util.Map;

@Service
public class CalendarMailService {

    private static final String CALENDAR = "calendar";
    private static final String EXCEPTION = "exception";

    private final MailService mailService;

    CalendarMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Send an email to the tool's manager if an error occurs during adding calendar event.
     *
     * @param calendarName that is used for syncing
     * @param absence      represents the absence of a person
     * @param exception    describes the error
     */
    public void sendCalendarSyncErrorNotification(String calendarName, Absence absence, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put(CALENDAR, calendarName);
        model.put("absence", absence);
        model.put(EXCEPTION, exception);

        mailService.sendTechnicalMail("subject.error.calendar.sync", "error_calendar_sync", model);
    }

    /**
     * Send an email to the tool's manager if an error occurs during update of calendar event.
     *
     * @param calendarName that is used for syncing
     * @param absence      represents the absence of a person
     * @param eventId      unique calendar event id
     * @param exception    describes the error
     */
    public void sendCalendarUpdateErrorNotification(String calendarName, Absence absence, String eventId,
                                                    String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put(CALENDAR, calendarName);
        model.put("absence", absence);
        model.put("eventId", eventId);
        model.put(EXCEPTION, exception);

        mailService.sendTechnicalMail("subject.error.calendar.update", "error_calendar_update", model);
    }

    /**
     * Send an email to the tool's manager if an error occurs during syncing delete action to calendar.
     *
     * @param calendarName name of calendar that is used for syncing
     * @param eventId      id of event which should be deleted
     * @param exception    describes the error
     */
    public void sendCalendarDeleteErrorNotification(String calendarName, String eventId, String exception) {

        Map<String, Object> model = new HashMap<>();
        model.put(CALENDAR, calendarName);
        model.put("eventId", eventId);
        model.put(EXCEPTION, exception);

        mailService.sendTechnicalMail("subject.error.calendar.delete", "error_calendar_delete", model);
    }
}
