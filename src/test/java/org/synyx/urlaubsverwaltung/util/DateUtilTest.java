
package org.synyx.urlaubsverwaltung.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;


/**
 * Unit test for {@link DateUtil}.
 */
public class DateUtilTest {

    @Test
    public void ensureReturnsTrueIfGivenDayIsAWorkDay() {

        // Monday
        LocalDate date = LocalDate.of(2011, 12, 26);

        boolean returnValue = DateUtil.isWorkDay(date);

        Assert.assertTrue("Should return true for a work day", returnValue);
    }


    @Test
    public void ensureReturnsFalseIfGivenDayIsNotAWorkDay() {

        // Sunday
        LocalDate date = LocalDate.of(2014, 11, 23);

        boolean returnValue = DateUtil.isWorkDay(date);

        Assert.assertFalse("Should return false for not a work day", returnValue);
    }


    @Test
    public void ensureReturnsCorrectFirstDayOfMonth() {

        int year = 2014;
        int month = 11;

        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);

        Assert.assertEquals("Not the correct first day of month", firstDayOfMonth,
            DateUtil.getFirstDayOfMonth(year, month));
    }


    @Test
    public void ensureReturnsCorrectLastDayOfMonth() {

        int year = 2014;
        int month = 11;

        LocalDate lastDayOfMonth = LocalDate.of(year, month, 30);

        Assert.assertEquals("Not the correct last day of month", lastDayOfMonth,
            DateUtil.getLastDayOfMonth(year, month));
    }


    @Test
    public void ensureReturnsCorrectLastDayOfMonthForSpecialMonths() {

        int year = 2014;
        int month = 2;

        LocalDate lastDayOfMonth = LocalDate.of(year, month, 28);

        Assert.assertEquals("Not the correct last day of month", lastDayOfMonth,
            DateUtil.getLastDayOfMonth(year, month));
    }


    @Test
    public void ensureReturnsCorrectFirstDayOfYear() {

        int year = 2014;

        LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);

        Assert.assertEquals("Not the correct first day of year", firstDayOfYear, DateUtil.getFirstDayOfYear(year));
    }


    @Test
    public void ensureReturnsCorrectLastDayOfYear() {

        int year = 2014;

        LocalDate lastDayOfYear = LocalDate.of(year, 12, 31);

        Assert.assertEquals("Not the correct last day of year", lastDayOfYear, DateUtil.getLastDayOfYear(year));
    }


    @Test
    public void ensureReturnsTrueForChristmasEve() {

        LocalDate date = LocalDate.of(2011, 12, 24);

        boolean returnValue = DateUtil.isChristmasEve(date);

        Assert.assertTrue("Should return true for 24th December", returnValue);
    }


    @Test
    public void ensureReturnsFalseForNotChristmasEve() {

        LocalDate date = LocalDate.of(2011, 12, 25);

        boolean returnValue = DateUtil.isChristmasEve(date);

        Assert.assertFalse("Should return false for 25th December", returnValue);
    }


    @Test
    public void ensureReturnsTrueForNewYearsEve() {

        LocalDate date = LocalDate.of(2014, 12, 31);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        Assert.assertTrue("Should return true for 31st December", returnValue);
    }


    @Test
    public void ensureReturnsFalseForNotNewYearsEve() {

        LocalDate date = LocalDate.of(2011, 12, 25);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        Assert.assertFalse("Should return false for 25th December", returnValue);
    }
}
