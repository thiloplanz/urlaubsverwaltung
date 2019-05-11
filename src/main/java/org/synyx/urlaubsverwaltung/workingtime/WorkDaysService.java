package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateFormat;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


/**
 * Service for calendar purpose.
 */
@Service
public class WorkDaysService {

    private final PublicHolidaysService publicHolidaysService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;

    @Autowired
    public WorkDaysService(PublicHolidaysService publicHolidaysService, WorkingTimeService workingTimeService,
        SettingsService settingsService) {

        this.publicHolidaysService = publicHolidaysService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
    }

    /**
     * This method calculates how many weekdays are between declared start date and end date
     * (official holidays are ignored here)
     *
     * Note: the start date must be before or equal the end date; this is validated prior to that method
     *
     * @param  startDate the first day of the time period to calculate workdays
     * @param  endDate the last day of the time period to calculate workdays
     *
     * @return  number of weekdays
     */
    public double getWeekDays(LocalDate startDate, LocalDate endDate) {

        double workDays = 0.0;

        if (!startDate.equals(endDate)) {
            LocalDate day = startDate;

            while (!day.isAfter(endDate)) {
                if (DateUtil.isWorkDay(day)) {
                    workDays++;
                }

                day = day.plusDays(1);
            }
        } else {
            if (DateUtil.isWorkDay(startDate)) {
                workDays++;
            }
        }

        return workDays;
    }


    /**
     * This method calculates how many workdays are used in the stated period (from start date to end date) considering
     * the personal working time of the given person, getNumberOfPublicHolidays calculates the number of official
     * holidays within the personal workdays period. Number of workdays results from difference between personal
     * workdays and official holidays.
     *
     * @param  dayLength personal daily working time of the given person
     * @param  startDate start day of the period to calculate the working days
     * @param  endDate last day of the period to calculate the working days
     * @param  person to calculate workdays in a certain time period
     *
     * @return  number of workdays in a certain time period
     */
    public BigDecimal getWorkDays(DayLength dayLength, LocalDate startDate, LocalDate endDate, Person person) {

        Optional<WorkingTime> optionalWorkingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(
                person, startDate);

        if (!optionalWorkingTime.isPresent()) {
            throw new NoValidWorkingTimeException("No working time found for User '" + person.getLoginName()
                + "' in period " + startDate.format(DateTimeFormatter.ofPattern(DateFormat.PATTERN)) + " - "
                + endDate.format(DateTimeFormatter.ofPattern(DateFormat.PATTERN)));
        }

        WorkingTime workingTime = optionalWorkingTime.get();

        FederalState federalState = getFederalState(workingTime);

        BigDecimal vacationDays = BigDecimal.ZERO;

        LocalDate day = startDate;

        while (!day.isAfter(endDate)) {
            // value may be 1 for public holiday, 0 for not public holiday or 0.5 for Christmas Eve or New Year's Eve
            BigDecimal duration = publicHolidaysService.getWorkingDurationOfDate(day, federalState);

            int dayOfWeek = day.getDayOfWeek().getValue();
            BigDecimal workingDuration = workingTime.getDayLengthForWeekDay(dayOfWeek).getDuration();

            BigDecimal result = duration.multiply(workingDuration);

            vacationDays = vacationDays.add(result);

            day = day.plusDays(1);
        }

        // vacation days < 1 day --> must not be divided, else an ArithmeticException is thrown
        if (vacationDays.compareTo(BigDecimal.ONE) < 0) {
            return vacationDays.setScale(1);
        }

        return vacationDays.multiply(dayLength.getDuration()).setScale(1);
    }


    private FederalState getFederalState(WorkingTime workingTime) {

        if (workingTime.getFederalStateOverride().isPresent()) {
            return workingTime.getFederalStateOverride().get();
        }

        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }
}
