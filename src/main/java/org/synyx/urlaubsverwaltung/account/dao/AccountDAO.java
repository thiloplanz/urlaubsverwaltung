
package org.synyx.urlaubsverwaltung.account.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.person.Person;


/**
 * Repository for {@link org.synyx.urlaubsverwaltung.account.domain.Account} entities.
 */
public interface AccountDAO extends CrudRepository<Account, Integer> {

    @Query("select x from Account x where YEAR(x.validFrom) = ?1 and x.person = ?2")
    Account getHolidaysAccountByYearAndPerson(int year, Person person);
}
