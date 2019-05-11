package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.OverlapService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * This service calculates if a {@link Person} may apply for leave, i.e. if he/she has enough vacation days to apply for
 * leave.
 */
@Service
public class CalculationService {

    private static final Logger LOG = LoggerFactory.getLogger(CalculationService.class);

    private final VacationDaysService vacationDaysService;
    private final AccountInteractionService accountInteractionService;
    private final AccountService accountService;
    private final WorkDaysService calendarService;
    private final OverlapService overlapService;

    @Autowired
    public CalculationService(VacationDaysService vacationDaysService, AccountService accountService,
                              AccountInteractionService accountInteractionService, WorkDaysService calendarService,
                              OverlapService overlapService) {

        this.vacationDaysService = vacationDaysService;
        this.accountService = accountService;
        this.accountInteractionService = accountInteractionService;
        this.calendarService = calendarService;
        this.overlapService = overlapService;
    }

    /**
     * Checks if applying for leave is possible, i.e. there are enough vacation days left to be used for the given
     * {@link org.synyx.urlaubsverwaltung.core.application.domain.Application} for leave.
     *
     * @param application for leave to check
     * @return {@code true} if the {@link org.synyx.urlaubsverwaltung.core.application.domain.Application} for leave
     * may be saved because there are enough vacation days left, {@code false} else
     */
    public boolean checkApplication(Application application) {

        Person person = application.getPerson();

        DayLength dayLength = application.getDayLength();

        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();
        int yearOfStartDate = startDate.getYear();
        int yearOfEndDate = endDate.getYear();

        if (yearOfStartDate == yearOfEndDate) {
            BigDecimal workDays = calendarService.getWorkDays(dayLength, startDate, endDate, person);

            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDays, application);
        } else {
            // ensure that applying for leave for the period in the old year is possible
            BigDecimal workDaysInOldYear = calendarService.getWorkDays(dayLength, startDate,
                DateUtil.getLastDayOfYear(yearOfStartDate), person);

            // ensure that applying for leave for the period in the new year is possible
            BigDecimal workDaysInNewYear = calendarService.getWorkDays(dayLength,
                DateUtil.getFirstDayOfYear(yearOfEndDate), endDate, person);

            return accountHasEnoughVacationDaysLeft(person, yearOfStartDate, workDaysInOldYear, application)
                && accountHasEnoughVacationDaysLeft(person, yearOfEndDate, workDaysInNewYear, application);
        }
    }


    private boolean accountHasEnoughVacationDaysLeft(Person person, int year, BigDecimal workDays,
                                                     Application application) {

        Optional<Account> account = getHolidaysAccount(year, person);

        if (!account.isPresent()) {
            return false;
        }

        // we also need to look at the next year, because "remaining days" from this year
        // may already have been booked then

        // call accountService directly to avoid auto-creating a new account for next year
        Optional<Account> nextYear = accountService.getHolidaysAccount(year + 1, person);
        BigDecimal alreadyUsedNextYear = vacationDaysService.getRemainingVacationDaysAlreadyUsed(nextYear);

        VacationDaysLeft vacationDaysLeft = vacationDaysService.getVacationDaysLeft(account.get(), nextYear);
        LOG.info("vacationDaysLeft: {} {}", year + 1, vacationDaysLeft);

        // now we need to consider which remaining vacation days expire
        BigDecimal workDaysBeforeApril = getWorkdaysBeforeApril(year, application);

        BigDecimal leftUntilApril = vacationDaysLeft.getVacationDays()
            .add(vacationDaysLeft.getRemainingVacationDays())
            .subtract(workDaysBeforeApril)
            .subtract(alreadyUsedNextYear);

        BigDecimal workDaysAfterApril = workDays.subtract(workDaysBeforeApril);
        BigDecimal leftAfterApril = vacationDaysLeft.getRemainingVacationDays()
            .add(leftUntilApril)
            .subtract(workDaysAfterApril)
            .subtract(vacationDaysLeft.getRemainingVacationDaysNotExpiring());

        if (leftUntilApril.signum() < 0 || leftAfterApril.signum() < 0) {
            if (alreadyUsedNextYear.signum() > 0) {
                LOG.info("Rejecting application by {} for {} days in because remaining days " +
                    "have already been used in {}", person, workDays, year, alreadyUsedNextYear, year + 1);
            }
            return false;
        }

        return true;
    }

    private BigDecimal getWorkdaysBeforeApril(int year, Application application) {
        List<Interval> beforeApril = overlapService.getListOfOverlaps(
            DateUtil.getFirstDayOfYear(year),
            DateUtil.getLastDayOfMonth(year, DateTimeConstants.MARCH),
            Collections.singletonList(application),
            Collections.emptyList()
        );

        return beforeApril.isEmpty() ? BigDecimal.ZERO : calculateWorkDaysBeforeApril(application, beforeApril);
    }

    private BigDecimal calculateWorkDaysBeforeApril(Application application, List<Interval> beforeApril) {
        return calendarService.getWorkDays(
            application.getDayLength(),
            beforeApril.get(0).getStart().toDateMidnight(),
            beforeApril.get(0).getEnd().toDateMidnight(),
            application.getPerson());
    }


    private Optional<Account> getHolidaysAccount(int year, Person person) {

        Optional<Account> holidaysAccount = accountService.getHolidaysAccount(year, person);

        if (holidaysAccount.isPresent()) {
            return holidaysAccount;
        }

        Optional<Account> lastYearsHolidaysAccount = accountService.getHolidaysAccount(year - 1, person);
        return lastYearsHolidaysAccount.map(accountInteractionService::autoCreateOrUpdateNextYearsHolidaysAccount);

    }
}
