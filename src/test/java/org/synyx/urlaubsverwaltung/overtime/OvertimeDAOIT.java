package org.synyx.urlaubsverwaltung.overtime;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDAO;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;


@RunWith(SpringRunner.class)
@DataJpaTest
public class OvertimeDAOIT {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private OvertimeDAO overtimeDAO;

    @Test
    public void ensureCanPersistOvertime() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        LocalDate now = LocalDate.now(UTC);
        Overtime overtime = new Overtime(person, now, now.plusDays(2), BigDecimal.ONE);

        Assert.assertNull("Must not have ID", overtime.getId());

        overtimeDAO.save(overtime);

        Assert.assertNotNull("Missing ID", overtime.getId());
    }


    @Test
    public void ensureCountsTotalHoursCorrectly() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        Person otherPerson = TestDataCreator.createPerson();
        personDAO.save(otherPerson);

        LocalDate now = LocalDate.now(UTC);

        // Overtime for person
        overtimeDAO.save(new Overtime(person, now, now.plusDays(2), new BigDecimal("3")));
        overtimeDAO.save(new Overtime(person, now.plusDays(5), now.plusDays(10), new BigDecimal("0.5")));
        overtimeDAO.save(new Overtime(person, now.minusDays(8), now.minusDays(4), new BigDecimal("-1")));

        // Overtime for other person
        overtimeDAO.save(new Overtime(otherPerson, now.plusDays(5), now.plusDays(10), new BigDecimal("5")));

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        Assert.assertEquals("Total hours calculated wrongly", new BigDecimal("2.5").setScale(1,
                BigDecimal.ROUND_UNNECESSARY), totalHours.setScale(1, BigDecimal.ROUND_UNNECESSARY));
    }


    @Test
    public void ensureReturnsNullAsTotalOvertimeIfPersonHasNoOvertimeRecords() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        BigDecimal totalHours = overtimeDAO.calculateTotalHoursForPerson(person);

        Assert.assertNull("Should be null", totalHours);
    }


    @Test
    public void ensureReturnsAllRecordsWithStartOrEndDateInTheGivenYear() {

        Person person = TestDataCreator.createPerson();
        personDAO.save(person);

        // records for 2015
        overtimeDAO.save(new Overtime(person, LocalDate.of(2014, 12, 30), LocalDate.of(2015, 1, 3),
                new BigDecimal("1")));
        overtimeDAO.save(new Overtime(person, LocalDate.of(2015, 10, 5), LocalDate.of(2015, 10, 20),
                new BigDecimal("2")));
        overtimeDAO.save(new Overtime(person, LocalDate.of(2015, 12, 28), LocalDate.of(2016, 1, 6),
                new BigDecimal("3")));

        // record for 2014
        overtimeDAO.save(new Overtime(person, LocalDate.of(2014, 12, 5), LocalDate.of(2014, 12, 31),
                new BigDecimal("4")));

        List<Overtime> records = overtimeDAO.findByPersonAndPeriod(person,
            LocalDate.of(2015, 1, 1),LocalDate.of(2015, 12, 31));

        Assert.assertNotNull("Should not be null", records);
        Assert.assertEquals("Wrong number of records", 3, records.size());
        Assert.assertEquals("Wrong record", new BigDecimal("1"), records.get(0).getHours());
        Assert.assertEquals("Wrong record", new BigDecimal("2"), records.get(1).getHours());
        Assert.assertEquals("Wrong record", new BigDecimal("3"), records.get(2).getHours());
    }
}
