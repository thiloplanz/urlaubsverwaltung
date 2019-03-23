package org.synyx.urlaubsverwaltung.core.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import java.util.Optional;


/**
 * Implementation of {@link CalendarSyncService}.
 */
@Service
public class CalendarSyncServiceImpl implements CalendarSyncService {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarSyncServiceImpl.class);

    private final SettingsService settingsService;
    private final CalendarService calendarService;

    @Autowired
    public CalendarSyncServiceImpl(SettingsService settingsService, CalendarService calendarService) {
        this.settingsService = settingsService;
        this.calendarService = calendarService;

        LOG.info("The following calendar provider is configured: {}", calendarService.getCalendarProvider().getClass());
    }

    @Override
    public Optional<String> addAbsence(Absence absence) {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        return calendarService.getCalendarProvider().add(absence, calendarSettings);
    }


    @Override
    public void update(Absence absence, String eventId) {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        calendarService.getCalendarProvider().update(absence, eventId, calendarSettings);
    }


    @Override
    public void deleteAbsence(String eventId) {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        calendarService.getCalendarProvider().delete(eventId, calendarSettings);
    }

    @Override
    public void checkCalendarSyncSettings() {
        CalendarSettings calendarSettings = this.settingsService.getSettings().getCalendarSettings();

        calendarService.getCalendarProvider().checkCalendarSyncSettings(calendarSettings);
    }
}
