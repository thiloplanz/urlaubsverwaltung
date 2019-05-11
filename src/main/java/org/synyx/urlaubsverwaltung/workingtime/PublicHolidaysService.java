package org.synyx.urlaubsverwaltung.workingtime;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Service for calendar purpose using jollyday library.
 */
@Component
public class PublicHolidaysService {

    private final HolidayManager manager;
    private final SettingsService settingsService;

    @Autowired
    public PublicHolidaysService(SettingsService settingsService, HolidayManager holidayManager) {

        this.settingsService = settingsService;
        this.manager = holidayManager;
    }

    /**
     * Returns the working duration for a date: may be full day (1.0) for a non public holiday or zero (0.0) for a
     * public holiday. The working duration for Christmas Eve and New Year's Eve are configured in the business
     * properties; normally the working duration for these holidays is a half day (0.5)
     *
     * @param  date  to get working duration for
     * @param  federalState  the federal state to consider holiday settings for
     *
     * @return  working duration of the given date
     */
    public BigDecimal getWorkingDurationOfDate(LocalDate date, FederalState federalState) {

        return getAbsenceTypeOfDate(date, federalState).getInverse().getDuration();
    }

    public DayLength getAbsenceTypeOfDate(LocalDate date, FederalState federalState) {

        Settings settings = settingsService.getSettings();
        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();

        DayLength workingTime = DayLength.FULL;

        if (isPublicHoliday(date, federalState)) {
            if (DateUtil.isChristmasEve(date)) {
                workingTime = workingTimeSettings.getWorkingDurationForChristmasEve();
            } else if (DateUtil.isNewYearsEve(date)) {
                workingTime = workingTimeSettings.getWorkingDurationForNewYearsEve();
            } else {
                workingTime = DayLength.ZERO;
            }
        }

        return workingTime.getInverse();
    }


    public Set<Holiday> getHolidays(int year, FederalState federalState) {

        return manager.getHolidays(year, federalState.getCodes());
    }


    public Set<Holiday> getHolidays(int year, final int month, FederalState federalState) {

        Set<Holiday> holidays = getHolidays(year, federalState);

        return holidays.stream().filter(byMonth(month)).collect(Collectors.toSet());
    }

    private boolean isPublicHoliday(LocalDate date, FederalState federalState) {

        return manager.isHoliday(date, federalState.getCodes());
    }

    private Predicate<Holiday> byMonth(int month) {

        return holiday -> holiday.getDate().getMonthValue() == month;
    }
}
