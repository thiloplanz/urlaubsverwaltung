package org.synyx.urlaubsverwaltung.core.account.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.joda.time.DateTimeConstants.DECEMBER;
import static org.joda.time.DateTimeConstants.JANUARY;
import static org.joda.time.DateTimeConstants.OCTOBER;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AccountInteractionServiceImplTest {

    private AccountInteractionServiceImpl sut;

    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;

    private Person person;


    @Before
    public void setup() {

        sut = new AccountInteractionServiceImpl(accountService, vacationDaysService);

        person = TestDataCreator.createPerson("horscht");
    }


    @Test
    public void testUpdateRemainingVacationDays() {

        DateMidnight startDate = new DateMidnight(2012, JANUARY, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Account account2012 = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5), ZERO, null);

        Account account2013 = new Account(person, startDate.withYear(2013).toDate(), endDate.withYear(2013).toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(3), ZERO, "comment1");

        Account account2014 = new Account(person, startDate.withYear(2014).toDate(), endDate.withYear(2014).toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(8), ZERO, "comment2");

        when(accountService.getHolidaysAccount(2012, person)).thenReturn(Optional.of(account2012));
        when(accountService.getHolidaysAccount(2013, person)).thenReturn(Optional.of(account2013));
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account2014));
        when(accountService.getHolidaysAccount(2015, person)).thenReturn(Optional.empty());

        when(vacationDaysService.calculateTotalLeftVacationDays(account2012)).thenReturn(BigDecimal.valueOf(6));
        when(vacationDaysService.calculateTotalLeftVacationDays(account2013)).thenReturn(BigDecimal.valueOf(2));

        sut.updateRemainingVacationDays(2012, person);

        verify(vacationDaysService).calculateTotalLeftVacationDays(account2012);
        verify(vacationDaysService).calculateTotalLeftVacationDays(account2013);
        verify(vacationDaysService, never()).calculateTotalLeftVacationDays(account2014);

        verify(accountService, never()).save(account2012);
        verify(accountService).save(account2013);
        verify(accountService).save(account2014);

        Assert.assertEquals("Wrong number of remaining vacation days for 2012", BigDecimal.valueOf(5),
                account2012.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days for 2013", BigDecimal.valueOf(6),
                account2013.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days for 2014", BigDecimal.valueOf(2),
                account2014.getRemainingVacationDays());
        Assert.assertEquals("Wrong comment", null, account2012.getComment());
        Assert.assertEquals("Wrong comment", "comment1", account2013.getComment());
        Assert.assertEquals("Wrong comment", "comment2", account2014.getComment());
    }


    @Test
    public void ensureCreatesNewHolidaysAccountIfNotExistsYet() {

        int year = 2014;
        int nextYear = 2015;

        DateMidnight startDate = new DateMidnight(year, JANUARY, 1);
        DateMidnight endDate = new DateMidnight(year, OCTOBER, 31);
        BigDecimal leftDays = BigDecimal.ONE;

        Account referenceHolidaysAccount = new Account(person, startDate.toDate(), endDate.toDate(),
                BigDecimal.valueOf(30), BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.empty());
        when(vacationDaysService.calculateTotalLeftVacationDays(referenceHolidaysAccount)).thenReturn(leftDays);

        Account createdHolidaysAccount = sut.autoCreateOrUpdateNextYearsHolidaysAccount(referenceHolidaysAccount);

        Assert.assertNotNull("Should not be null", createdHolidaysAccount);

        Assert.assertEquals("Wrong person", person, createdHolidaysAccount.getPerson());
        Assert.assertEquals("Wrong number of annual vacation days", referenceHolidaysAccount.getAnnualVacationDays(),
                createdHolidaysAccount.getAnnualVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", leftDays,
                createdHolidaysAccount.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of not expiring remaining vacation days", ZERO,
                createdHolidaysAccount.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Wrong validity start date", new DateMidnight(nextYear, 1, 1),
                createdHolidaysAccount.getValidFrom());
        Assert.assertEquals("Wrong validity end date", new DateMidnight(nextYear, 12, 31),
                createdHolidaysAccount.getValidTo());

        verify(accountService).save(createdHolidaysAccount);

        verify(vacationDaysService).calculateTotalLeftVacationDays(referenceHolidaysAccount);
        verify(accountService, times(2)).getHolidaysAccount(nextYear, person);
    }


    @Test
    public void ensureUpdatesRemainingVacationDaysOfHolidaysAccountIfAlreadyExists() {

        int year = 2014;
        int nextYear = 2015;

        DateMidnight startDate = new DateMidnight(year, JANUARY, 1);
        DateMidnight endDate = new DateMidnight(year, OCTOBER, 31);
        BigDecimal leftDays = BigDecimal.valueOf(7);

        Account referenceAccount = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(30),
                BigDecimal.valueOf(8), BigDecimal.valueOf(4), "comment");

        Account nextYearAccount = new Account(person, new DateMidnight(nextYear, 1, 1).toDate(), new DateMidnight(
                nextYear, 10, 31).toDate(), BigDecimal.valueOf(28), ZERO, ZERO, "comment");

        when(accountService.getHolidaysAccount(nextYear, person)).thenReturn(Optional.of(nextYearAccount));
        when(vacationDaysService.calculateTotalLeftVacationDays(referenceAccount)).thenReturn(leftDays);

        Account account = sut.autoCreateOrUpdateNextYearsHolidaysAccount(referenceAccount);

        Assert.assertNotNull("Should not be null", account);

        Assert.assertEquals("Wrong person", person, account.getPerson());
        Assert.assertEquals("Wrong number of annual vacation days", nextYearAccount.getAnnualVacationDays(),
                account.getAnnualVacationDays());
        Assert.assertEquals("Wrong number of remaining vacation days", leftDays, account.getRemainingVacationDays());
        Assert.assertEquals("Wrong number of not expiring remaining vacation days", ZERO,
                account.getRemainingVacationDaysNotExpiring());
        Assert.assertEquals("Wrong validity start date", nextYearAccount.getValidFrom(), account.getValidFrom());
        Assert.assertEquals("Wrong validity end date", nextYearAccount.getValidTo(), account.getValidTo());

        verify(accountService).save(account);

        verify(vacationDaysService).calculateTotalLeftVacationDays(referenceAccount);
        verify(accountService).getHolidaysAccount(nextYear, person);
    }

    @Test
    public void createHolidaysAccount() {

        DateMidnight validFrom = new DateMidnight(2014, JANUARY, 1);
        DateMidnight validTo = new DateMidnight(2014, DECEMBER, 31);

        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.empty());

        Account expectedAccount = sut.updateOrCreateHolidaysAccount(person, validFrom, validTo, TEN, ONE, ZERO, TEN, "comment");
        assertThat(expectedAccount.getPerson()).isEqualTo(person);
        assertThat(expectedAccount.getAnnualVacationDays()).isEqualTo(TEN);
        assertThat(expectedAccount.getVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getRemainingVacationDays()).isSameAs(ZERO);
        assertThat(expectedAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(TEN);

        verify(accountService).save(expectedAccount);
    }

    @Test
    public void updateHolidaysAccount() {

        DateMidnight validFrom = new DateMidnight(2014, JANUARY, 1);
        DateMidnight validTo = new DateMidnight(2014, DECEMBER, 31);
        Account account = new Account(person, validFrom.toDate(), validTo.toDate(), TEN, TEN, TEN, "comment");
        when(accountService.getHolidaysAccount(2014, person)).thenReturn(Optional.of(account));

        Account expectedAccount = sut.updateOrCreateHolidaysAccount(person, validFrom, validTo, ONE, ONE, ONE, ONE, "new comment");
        assertThat(expectedAccount.getPerson()).isEqualTo(person);
        assertThat(expectedAccount.getAnnualVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getVacationDays()).isEqualTo(ONE);
        assertThat(expectedAccount.getRemainingVacationDays()).isSameAs(ONE);
        assertThat(expectedAccount.getRemainingVacationDaysNotExpiring()).isEqualTo(ONE);

        verify(accountService).save(expectedAccount);
    }
}
