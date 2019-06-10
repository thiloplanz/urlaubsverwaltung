package org.synyx.urlaubsverwaltung.calendarintegration.providers.noop;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class NoopCalendarSyncProvider implements CalendarProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Override
    public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {

        LOG.info("No calendar provider configured to add event: {}", absence);

        return Optional.empty();
    }

    @Override
    public void update(Absence absence, String eventId, CalendarSettings calendarSettings) {

        LOG.info("No calendar provider configured to update event: {}, eventId {}", absence, eventId);
    }

    @Override
    public void delete(String eventId, CalendarSettings calendarSettings) {
        LOG.info("No calendar provider configured to delete event '{}'", eventId);
    }

    @Override
    public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {
        LOG.info("No calendar provider configured to check calendarSettings '{}'", calendarSettings);
    }
}
