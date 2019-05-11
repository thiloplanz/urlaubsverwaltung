package org.synyx.urlaubsverwaltung.account.dao;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDAO;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;

import static java.math.BigDecimal.TEN;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;

@RunWith(SpringRunner.class)
@DataJpaTest
public class AccountDAOTest {

    @Autowired
    private AccountDAO sut;

    @Autowired
    private PersonDAO personDAO;

    @Test(expected = DataIntegrityViolationException.class)
    public void ensureUniqueConstraintOfPersonAndValidFrom() {

        final Person person = TestDataCreator.createPerson("test user");
        person.setId(1);
        personDAO.save(person);

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        Account account = new Account(person, validFrom, validTo, TEN, TEN, TEN, "comment");
        sut.save(account);

        final LocalDate validFrom2 = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo2 = LocalDate.of(2014, DECEMBER, 31);
        Account account2 = new Account(person, validFrom2, validTo2, TEN, TEN, TEN, "comment 2");
        sut.save(account2);
    }
}
